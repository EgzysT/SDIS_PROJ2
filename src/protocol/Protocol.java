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
            Logger.fine("Store", "peer already has chunk #" + chunkNo + " from file " + fileID);
            connection.reply(true);
            return;
        }

//        if (Store.hasChunkReplica(fileID, chunkNo, i)) {
//            Logger.fine("Store", "peer already has chunk #" + chunkNo + " from file " + fileID);
//            connection.reply(false);
//            return;
//        }

        // Check if peer has enough space for chunk
        if (Peer.instance().currentDiskSpace.get() + chunk.length > Peer.instance().maxDiskSpace.get()) {
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
     * Looks up for chunk
     * @param fileID File identifier
     * @param chunkNo Chunk number
     * @return Chunk
     */
    public static void readChunk(ProtocolConnection connection, String fileID, Integer chunkNo) {

        // Check if peer has chunk
        if (!Store.hasChunk(fileID, chunkNo)) {
            Logger.fine("Store", "peer does not have chunk #" + chunkNo + " from file " + fileID + " stored");
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
     * Deletes chunk
     * @param fileID
     * @param chunkNo
     * @return true if found and deleted
     */
    public static void deleteChunk(String fileID, Integer chunkNo, Integer i) {

//        // Check if peer has chunk
//        if (!Store.hasChunkReplica(fileID, chunkNo, i)) {
//            Logger.fine("Store", "peer does not have chunk #" + chunkNo + " from file " + fileID + " stored");
//            return false;
//        }

        Store.unregisterChunk(fileID, chunkNo, i);

        // TODO check
        if (!Store.hasChunk(fileID, chunkNo)) {

//            System.out.println("Deleting  chunk #" + chunkNo + " replica " + i);

            try {
                if (!Files.deleteIfExists(Paths.get(Peer.instance().backupDir + File.separator + fileID + File.separator + chunkNo + ".chunk"))) {
                    Logger.fine("Store", "peer does not have chunk #" + chunkNo + " from file " + fileID + " on disk");
//                    return false;
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }

//        return true;
    }
}



