package protocol;

import chord.ChordNode;
import peer.Peer;
import store.Store;
import utils.Logger;

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

public class Restore {

    static Map<String , Boolean> instances;

    static {
        instances = new ConcurrentHashMap<>();
    }

    private Restore() {}

    private static Boolean checkRequirements(String filePath, String fileID) {

        if (fileID == null) {
            Logger.fine("Restore", "file " + filePath + " is not backed up by this peer");
            return false;
        }

        // Check if file is free
        if (ProtocolHandler.isFileBusy(fileID)) {
            Logger.warning("Restore", "found another protocol instance for file " + fileID);
            return false;
        }

        return true;
    }

    public static void restoreFile(String filePath) {

        String fileID = Store.getFile(filePath);

        if (!checkRequirements(filePath, fileID))
            return;

        if (instances.putIfAbsent(fileID, true) != null) {
            Logger.warning("Restore", "found another restore protocol instance for file " + fileID);
            return;
        }

        // First chunk of file
        byte[] chunk = ChordNode.instance().get(fileID, 0);

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

                    if (chunkNo >= Store.files.get(fileID).chunks) {

                        try {
                            fileChannel.close();
                        } catch (IOException e) {
                            Logger.severe("Restore", "failed to close file channel");
                        }

                        instances.computeIfPresent(fileID, (k,v) -> null);

                        Logger.info("Restore", "completed restore protocol for file " + fileID);

                        return;
                    }

                    // TODO later check for errors
                    byte[] chunk = ChordNode.instance().get(fileID, chunkNo);

                    attachment = ByteBuffer.wrap(chunk);

                    fileChannel.write(attachment, chunkNo * 64000, attachment, this);
                }

                @Override
                public void failed(Throwable exc, ByteBuffer attachment) {
                    Logger.severe("Backup", "failed to write chunk");
                }
            });

        } catch (IOException e) {
            Logger.severe("Backup", "failed to write chunk");
        }
    }
}
