package protocol;

import chord.ChordNode;
import peer.Peer;
import utils.Logger;
import utils.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.file.StandardOpenOption.*;

/**
 * Restore protocol
 */
public abstract class Restore {

    /** Restore instances */
    static Map<String , Boolean> instances;

    static {
        instances = new ConcurrentHashMap<>();
    }

    /**
     * Checks requirements before restore
     * @param fileID File identifier
     * @return True if all requirements are met, false otherwise
     */
    private static Boolean checkRequirements(String fileID) {

        // Check if file is backed up
        if (!ProtocolHandler.isFileBackedUp(fileID)) {
            Logger.warning("Restore", "file " + fileID + " is not backed up");
            return false;
        }

        // Check if file is busy
        if (ProtocolHandler.isFileBusy(fileID)) {
            Logger.warning("Restore", "found another protocol instance for file " + fileID);
            return false;
        }

        return true;
    }

    /**
     * Restore file
     * @param filePath File path
     */
    public static void restoreFile(String filePath) {

        String fileID = Utils.generateFileID(filePath);

        if (!checkRequirements(fileID))
            return;

        if (instances.putIfAbsent(fileID, true) != null) {
            Logger.warning("Restore", "found another restore protocol instance for file " + fileID);
            return;
        }

        // First chunk of file
        byte[] chunk = ChordNode.instance().get(fileID, 0);

        if (chunk == null) {
            instances.computeIfPresent(fileID, (k,v) -> null);
            Logger.warning("Restore", "failed to restore chunk #0 from file " + fileID);
            return;
        }

        Logger.fine("Restore", "restored chunk #0 from file " + fileID);

        try {
            Path path = Paths.get(Peer.instance().restoreDir + File.separator + Paths.get(filePath).getFileName());
            AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(path, WRITE, CREATE);
            ByteBuffer buffer = ByteBuffer.wrap(chunk);

            fileChannel.write(buffer, 0, buffer, new CompletionHandler<Integer, ByteBuffer>() {

                private Integer chunkNo;

                {
                    chunkNo = 0;
                }

                @Override
                public void completed(Integer result, ByteBuffer attachment) {

                    chunkNo++;

                    if (result < 64000) {

                        try {
                            fileChannel.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                            System.exit(-1);
                        }

                        instances.computeIfPresent(fileID, (k,v) -> null);

                        Logger.info("Restore", "completed restore protocol for file " + fileID);

                        return;
                    }

                    byte[] chunk = ChordNode.instance().get(fileID, chunkNo);

                    if (chunk == null) {
                        instances.computeIfPresent(fileID, (k,v) -> null);
                        Logger.warning("Restore", "failed to restore chunk #" + chunkNo + " from file " + fileID);
                        return;
                    }

                    Logger.fine("Restore", "restored chunk #" + chunkNo + " from file " + fileID);

                    attachment = ByteBuffer.wrap(chunk);

                    fileChannel.write(attachment, chunkNo * 64000, attachment, this);
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
