package protocol;

import chord.ChordNode;
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

    /** Backup instances */
    static Map<String, Boolean> instances;

    static {
        instances = new ConcurrentHashMap<>();
    }

    /**
     * Checks requirements before backup
     * @param fileID File identifier
     * @return True if all requirements are met, false otherwise
     */
    private static Boolean checkRequirements(String fileID) {

        // Check if file is backed up
        if (ProtocolHandler.isFileBackedUp(fileID)) {
            Logger.warning("Backup", "file " + fileID + " is already backed up");
            return false;
        }

        // Check if file is busy
        if (ProtocolHandler.isFileBusy(fileID)) {
            Logger.warning("Backup", "found another protocol instance for file " + fileID);
            return false;
        }

        return true;
    }

    /**
     * Backup file
     * @param filePath File path
     */
    public static void backupFile(String filePath) {

        // Check if file exists
        if (!Files.exists(Paths.get(filePath))) {
            Logger.warning("Backup", "file " + filePath + " not found");
            return;
        }

        String fileID = Utils.generateFileID(filePath);

        if (!checkRequirements(fileID))
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
                            e.printStackTrace();
                            System.exit(-1);
                        }

                        if (fileSize % 64000 == 0) {

                            boolean status = ChordNode.instance().put(
                                    fileID,
                                    chunkNo,
                                    new byte[0]
                            );

                            if (!status) {
                                instances.computeIfPresent(fileID, (k,v) -> null);
                                Logger.warning("Backup", "failed to backup chunk #" + chunkNo + " from file " + fileID);
                                return;
                            }

                            Logger.fine("Backup", "backed up chunk #" + chunkNo + " from file " + fileID);

                            chunkNo++;
                        }

                        instances.computeIfPresent(fileID, (k,v) -> null);

                        Logger.info("Backup", "completed backup protocol for file " + fileID);

                        return;
                    }

                    attachment.flip();

                    boolean status = ChordNode.instance().put(
                            fileID, chunkNo,
                            Arrays.copyOfRange(attachment.array(), 0, attachment.remaining())
                    );

                    if (!status) {
                        instances.computeIfPresent(fileID, (k,v) -> null);
                        Logger.warning("Backup", "failed to backup chunk #" + chunkNo + " from file " + fileID);
                        return;
                    }

                    Logger.fine("Backup", "backed up chunk #" + chunkNo + " from file " + fileID);

                    attachment.clear();

                    chunkNo++;
                    chunkOffset += result;

                    fileChannel.read(attachment, chunkOffset, attachment, this);
                }

                @Override
                public void failed(Throwable exc, ByteBuffer attachment) {
                    exc.printStackTrace();
                    System.exit(-1);
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
