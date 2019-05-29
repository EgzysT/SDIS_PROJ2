package protocol;

import chord.ChordInfo;
import chord.ChordNode;
import common.protocol.ProtocolConnection;
import peer.Peer;
import store.Store;
import utils.Logger;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static java.nio.file.StandardOpenOption.*;

public abstract class Protocol {

    public static void writeChunk(ProtocolConnection connection, String fileID, Integer chunkNo, byte[] chunk, Integer i) {

        // Check if peer has chunk
        if (Store.hasChunk(fileID, chunkNo)) {
//            Logger.fine("Store", "peer already has chunk #" + chunkNo + " from file " + fileID);
            connection.reply(true);
            return;
        }

        // Check if peer has enough space for chunk
        if (Store.currentDiskSpace.get() + chunk.length > Store.maxDiskSpace.get()) {
            Logger.fine("Store", "peer does not have enough space for chunk #" + chunkNo + " from file " + fileID);
            connection.reply(false);
            return;
        }

        try {
            Path fileDir = Paths.get(Peer.instance().backupDir + File.separator + fileID + File.separator);
            Files.createDirectories(fileDir);

            Path chunkFile = Paths.get(fileDir + File.separator + chunkNo + ".chunk");
            AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(chunkFile, WRITE, CREATE);
            ByteBuffer buffer = ByteBuffer.wrap(chunk);

            fileChannel.write(buffer, 0, buffer, new CompletionHandler<Integer, ByteBuffer>() {

                @Override
                public void completed(Integer result, ByteBuffer attachment) {

                    try {
                        fileChannel.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.exit(-1);
                    }

                    Store.registerChunk(fileID, chunkNo, result, i);

                    connection.reply(true);
                }

                @Override
                public void failed(Throwable exc, ByteBuffer attachment) {
                    Logger.severe("Store", "failed to store chunk");
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * Reads chunk
     * @param fileID File identifier
     * @param chunkNo Chunk number
     * @return Chunk
     */
    public static void readChunk(ProtocolConnection connection, String fileID, Integer chunkNo) {

        // Check if peer has chunk
        if (!Store.hasChunk(fileID, chunkNo)) {
//            Logger.fine("Store", "peer does not have chunk #" + chunkNo + " from file " + fileID + " stored");
            connection.reply((byte[]) null);
            return;
        }

        try {
            Path path = Paths.get(Peer.instance().backupDir + File.separator + fileID + File.separator + chunkNo + ".chunk");
            AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(path, READ);
            ByteBuffer buffer = ByteBuffer.allocate(Store.chunks.get(fileID).get(chunkNo).size);

            fileChannel.read(buffer, 0, buffer, new CompletionHandler<Integer, ByteBuffer>() {
                @Override
                public void completed(Integer result, ByteBuffer attachment) {

                    attachment.flip();

                    connection.reply(Arrays.copyOfRange(attachment.array(), 0, attachment.remaining()));

                    attachment.clear();
                }

                @Override
                public void failed(Throwable exc, ByteBuffer attachment) {
                    Logger.severe("Protocol", "failed to recover chunk");
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public static Boolean transferChunk(InetSocketAddress node, String fileID, Integer chunkNo, Integer i) {

        // TODO add to instances?? to prevent other protocols from interfering

        // Check if peer has chunk
        if (!Store.hasChunk(fileID, chunkNo)) {
            Logger.fine("Store", "peer does not have chunk #" + chunkNo + " from file " + fileID + " stored");
            return false;
        }

        try {
            Path path = Paths.get(Peer.instance().backupDir + File.separator + fileID + File.separator + chunkNo + ".chunk");
            AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(path, READ);
            ByteBuffer buffer = ByteBuffer.allocate(Store.chunks.get(fileID).get(chunkNo).size);

            fileChannel.read(buffer, 0, buffer, new CompletionHandler<Integer, ByteBuffer>() {

                @Override
                public void completed(Integer result, ByteBuffer attachment) {

                    attachment.flip();

                    new ProtocolConnection(node).backupChunk(
                            fileID,
                            chunkNo,
                            Arrays.copyOfRange(attachment.array(), 0, attachment.remaining()),
                            i
                    );

                    attachment.clear();

                    deleteChunk(fileID, chunkNo, i);
                }

                @Override
                public void failed(Throwable exc, ByteBuffer attachment) {
                    Logger.severe("Protocol", "failed to recover chunk");
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        return true;
    }

    /**
     * Deletes chunk.
     * @param fileID File identifier
     * @param chunkNo Chunk number
     * @return True if deleted, false otherwise
     */
    public static Boolean deleteChunk(String fileID, Integer chunkNo, Integer i) {

        Store.unregisterChunk(fileID, chunkNo, i);

        if (!Store.hasChunk(fileID, chunkNo)) {

            try {
                return Files.deleteIfExists(Paths.get(Peer.instance().backupDir + File.separator + fileID + File.separator + chunkNo + ".chunk"));
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }

        return false;
    }
}



