package common.protocol;

import chord.ChordNode;
import common.Worker;
import peer.Peer;
import store.ChunkInfo;
import store.Store;
import utils.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.file.StandardOpenOption.*;

public class ProtocolWorker extends Worker {

    private ProtocolConnection connection;

    ProtocolWorker(ProtocolConnection connection) {
        this.connection = connection;
    }

    @Override
    public void work() {

        ProtocolMessage request = connection.listen();
//        System.out.println("Protocol request " + request);

        if (request == null)
            return;

        switch (request.type) {
            case BACKUP:
                if (!storeChunk(request.fileID, request.chunkNo, request.chunk))
                    connection.reply(false);
                break;
            case RESTORE:
                if (!recoverChunk(request.fileID, request.chunkNo))
                    connection.reply(false);
                break;
            case DELETE:
                if (!deleteChunk(request.fileID, request.chunkNo))
                    connection.reply(false);
                break;
            default:
                Logger.severe("Protocol", "invalid request received");
        }
    }

    private boolean storeChunk(String fileID, Integer chunkNo, byte[] chunk) {

        // TODO check if receiver != sender
        // Check if peer is initiator
//        if (files.containsKey(fileID)) {
//            Logger.fine("Store", "peer is initiator of file " + fileID);
//            return false;
//        }

        // Check if peer already has chunk
        if (Store.hasChunk(fileID, chunkNo, ChordNode.instance().id())) {
            Logger.fine("Store", "peer already has chunk #" + chunkNo + " from file " + fileID);
            return false;
        }

        // Check if peer has enough space for chunk
        if (Peer.instance().currentDiskSpace.get() + chunk.length > Peer.instance().maxDiskSpace.get()) {
            Logger.fine("Store", "peer does not have enough space for chunk #" + chunkNo + " from file " + fileID);
            return false;
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

                    connection.reply(true);

//                    Logger.fine("Store", "stored chunk #" + chunkNo + " from file #" + fileID);
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

        // TODO put this to Store
        Store.chunks.compute(fileID, (k1, v1) -> {
            if (v1 == null)
                v1 = new ConcurrentHashMap<>();

            v1.compute(chunkNo, (k2, v2) -> {
                if (v2 == null)
                    v2 = new ChunkInfo();

                // Update chunk info
                if (v2.peers.add(ChordNode.instance().id()))
                    Peer.instance().currentDiskSpace.addAndGet(chunk.length);

                v2.size = chunk.length;

                return v2;
            });

            return v1;
        });

        return true;
    }

    /**
     * Looks up for chunk
     * @param fileID File identifier
     * @param chunkNo Chunk number
     * @return Chunk
     */
    private Boolean recoverChunk(String fileID, Integer chunkNo) {

        // Check if peer has chunk
        if (!Store.hasChunk(fileID, chunkNo, ChordNode.instance().id())) {
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

                    connection.reply(Arrays.copyOfRange(attachment.array(), 0, attachment.remaining()));

                    attachment.clear();

//                    Logger.fine("Protocol", "recovered chunk #" + chunkNo + " from file #" + fileID);
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
     * TODO: possibly return the chunk that was stored.
     * @param fileID
     * @param chunkNo
     * @return true if found and deleted
     */
    private Boolean deleteChunk(String fileID, Integer chunkNo) {
        if (!Store.hasChunk(fileID, chunkNo, ChordNode.instance().id())) {
            Logger.fine("Store", "peer does not have chunk #" + chunkNo + " from file " + fileID + " stored");
            return false;
        }
        Store.unregisterChunk(fileID, chunkNo, ChordNode.instance().id());

        if (!Files.deleteIfExists(Paths.get(Peer.instance().backupDir + File.separator + fileID + File.separator + chunkNo + ".chunk"))){
            Logger.fine("Store", "peer does not have chunk #" + chunkNo + " from file " + fileID + " on disk");
            return false;
        }
        return true;
    }
}
