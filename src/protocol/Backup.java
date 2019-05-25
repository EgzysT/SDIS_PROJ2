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

import static java.nio.file.StandardOpenOption.READ;

// TODO replication, handle error in connection

public class Backup {

    static Map<String, Boolean> instances;

    static {
        instances = new ConcurrentHashMap<>();
    }

    private Backup() {}

    private static Boolean checkRequirements(String filePath, String fileID) {

        // Check if file exists
        if (!Files.exists(Paths.get(filePath))) {
            Logger.warning("Backup", "file" + filePath + " does not exist");
            return false;
        }

        // TODO node may not have backed up file or chunk
        // Check if file is already backed up
        if (Store.isBackedUp(fileID)) {
            Logger.warning("Backup", "file " + fileID + " is already backed up");
            return false;
        }

        // Check if file is free
        if (ProtocolHandler.isFileBusy(fileID)) {
            Logger.warning("Backup", "found another protocol instance for file " + fileID);
            return false;
        }

        // TODO Check if file is modified
        // Maybe use file data not in hash but in start of backup
        // To allow several nodes to recover it

        return true;
    }

    public static void backupFile(String filePath) {

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

                        Store.registerFile(fileID, filePath, chunkNo);

                        instances.computeIfPresent(fileID, (k,v) -> null);

//                        System.out.println();
//                        System.out.println(Store.debug());
//                        System.out.println();

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

                    // TODO test @ FEUP
//                    ProtocolHandler.schedule(
//                            () -> fileChannel.read(attachment, chunkOffset, attachment, this),
//                            100
//                    );
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
