package protocol;

import store.Store;
import utils.Logger;
import utils.Utils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.StandardOpenOption.READ;

public class Backup {

    private String filePath;

    public Backup(String filePath) {
        this.filePath = filePath;
    }

    private void init() {

        // Check if file exists
        if (!Files.exists(Paths.get(filePath))) {
            Logger.warning("Backup", "file" + filePath + " does not exist");
            return;
        }

        String fileID = Utils.generateFileID(filePath);

        // Check if file is already backed up
        if (Store.isBackedUp(fileID)) {
            Logger.fine("Backup", "file " + fileID + " is already backed up");
            return;
        }

        // Check if file is free


        // Check if file is modified

        Logger.info("Backup", "starting backup protocol for file " + fileID);

        splitFile(filePath);

    }

    private void splitFile(String filePath) {

        try {
            Path path = Paths.get(filePath);
            AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(path, READ);
            ByteBuffer buffer = ByteBuffer.allocate(64000);

            fileChannel.read(buffer, 0, buffer, new CompletionHandler<Integer, ByteBuffer>() {

                private Integer offset;

                {
                    offset = 0;
                }

                @Override
                public void completed(Integer result, ByteBuffer attachment) {

                    if (result == -1) {

                        try {
                            fileChannel.close();
                        } catch (IOException e) {
                            Logger.severe("Backup", "failed to close file channel");
                        }


                        // Register file

                        return;
                    }

                    attachment.flip();
                    backupChunk(attachment.array());
                    attachment.clear();

                    offset += result;

                    ProtocolHandler.schedule(
                            () -> fileChannel.read(attachment, offset, attachment, this),
                            100
                    );
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


    public void backupChunk(byte[] chunk) {



    }


}
