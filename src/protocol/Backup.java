package protocol;

import chord.ChordNode;
import store.Store;
import utils.Logger;
import utils.Utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.file.StandardOpenOption.*;

/**
 * Backup protocol
 */
public abstract class Backup {

    static Map<String, Boolean> instances;

    static {
        instances = new ConcurrentHashMap<>();
    }

    private static Boolean checkRequirements(String filePath, String fileID) {

        // TODO node may not have backed up file or chunk
        // Check if file is backed up
        if (Store.isBackedUp(fileID)) {
            Logger.warning("Backup", "file " + fileID + " is already backed up");
            return false;
        }

        // Check if file is busy
        if (ProtocolHandler.isFileBusy(fileID)) {
            Logger.warning("Backup", "found another protocol instance for file " + fileID);
            return false;
        }

        // Check if file is modified
        if (Store.getFileID(filePath) != null) {
            Logger.warning("Backup", "deleting old version of file " + filePath);
            Delete.deleteFile(filePath);
        }

        return true;
    }

    public static void backupFile(String filePath) {

        // Check if file exists
        if (!Files.exists(Paths.get(filePath))) {
            Logger.warning("Backup", "file " + filePath + " not found");
            return;
        }

        String fileID = Utils.generateFileID(filePath);

        if (!checkRequirements(filePath, fileID))
            return;

        if (instances.putIfAbsent(fileID, true) != null) {
            Logger.warning("Backup", "found another backup protocol instance for file " + fileID);
            return;
        }

        Logger.info("Backup", "starting backup protocol for file " + fileID);

        try {
            Path path = Paths.get(filePath);
            AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(path, READ);
            ByteBuffer buffer = ByteBuffer.allocate(64000);

            long fileSize = fileChannel.size();

            fileChannel.read(buffer, 0, buffer, new CompletionHandler<Integer, ByteBuffer>() {

                private Integer chunkNo;
                private Integer chunkOffset;

                {
                    chunkNo = 0;
                    chunkOffset = 0;
                }

                @Override
                public void completed(Integer result, ByteBuffer attachment) {

                    if (result == -1) {

                        try {
                            fileChannel.close();
                        } catch (IOException e) {
                            Logger.severe("Backup", "failed to close file channel");
                        }

                        if (fileSize % 64000 == 0) {

                            ChordNode.instance().put(
                                    fileID,
                                    chunkNo,
                                    new byte[0]
                            );

                            chunkNo++;
                        }

                        Store.registerFile(fileID, filePath, chunkNo);

                        instances.computeIfPresent(fileID, (k,v) -> null);

                        Logger.info("Backup", "completed backup protocol for file " + fileID);

                        return;
                    }

                    attachment.flip();

                    ChordNode.instance().put(
                            fileID,
                            chunkNo,
                            Arrays.copyOfRange(attachment.array(), 0, attachment.remaining())
                    );

                    attachment.clear();

                    chunkNo++;
                    chunkOffset += result;

                    fileChannel.read(attachment, chunkOffset, attachment, this);
                }

                @Override
                public void failed(Throwable exc, ByteBuffer attachment) {
                    Logger.severe("Backup", "failed to read chunk");
                }
            });

        } catch (IOException e) {
            Logger.severe("Backup", "failed to read chunk");
        }
    }
}
