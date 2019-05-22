package store;

import chord.ChordNode;
import peer.Peer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Store {

    public static Map<String, FileInfo> files;
    public static Map<String, Map<Integer, ChunkInfo>> chunks;

    static {
        files = new ConcurrentHashMap<>();
        chunks = new ConcurrentHashMap<>();
    }

    private Store() {}

    /**
     * Register a file that was backed up.
     * @param fileID            File ID
     * @param filePath          File path
     */
    public static void registerFile(String fileID, String filePath, Integer nrChunks) {
        files.putIfAbsent(fileID, new FileInfo(filePath, nrChunks));
    }

    /**
     * Register chunk that was stored by a peer.
     * @param fileID    File ID
     * @param chunkNo   Chunk number
     * @param nodeID    Node that stored chunk
     */
    public static void registerChunk(String fileID, Integer chunkNo, Integer nodeID) {
        chunks.compute(fileID, (k1, v1) -> {
            if (v1 == null)
                v1 = new ConcurrentHashMap<>();

            v1.compute(chunkNo, (k2, v2) -> {
                if (v2 == null)
                    v2 = new ChunkInfo();

                v2.peers.add(nodeID);

                return v2;
            });

            return v1;
        });
    }

   /**
    * Unregister chunk that was deleted by a peer.
    * @param fileID   File ID
    * @param chunkNo  Chunk number
    * @param id Peer that deleted chunk
    */
   public static void unregisterChunk(String fileID, Integer chunkNo, Integer id) {
       chunks.computeIfPresent(fileID, (k1, v1) -> {

           v1.computeIfPresent(chunkNo, (k2, v2) -> {
               v2.peers.remove(id);

               return v2;
           });

           return v1;
       });
   }

    // TODO
    // Peer backed up file
//    static boolean hasFile(String fileID) {
//        return files.containsKey(fileID);
//    }

    public static Boolean isBackedUp(String fileID) {
        return files.containsKey(fileID) || chunks.containsKey(fileID);
    }


    // Peer has chunk stored
    public static Boolean hasChunk(String fileID, Integer chunkNo, Integer peerID) {

        if (!chunks.containsKey(fileID) || !chunks.get(fileID).containsKey(chunkNo))
            return false;

        return chunks.get(fileID).get(chunkNo).peers.contains(peerID);
    }

    public static String getFile(String filePath) {

        Path fileName = Paths.get(filePath).getFileName();

        for (Map.Entry<String, FileInfo> fileInfo : files.entrySet()) {
            if (Paths.get(fileInfo.getValue().filePath).getFileName().equals(fileName))
                return fileInfo.getKey();
        }

        return null;
    }

    public static String debug() {
        StringBuilder sb = new StringBuilder();

        sb.append("- Backed up files:\n");

        for (Map.Entry<String, FileInfo> fileInfo : files.entrySet()) {
            sb.append("Path ").append(fileInfo.getValue().filePath).append("\n");
            sb.append("File ").append(fileInfo.getKey()).append("\n");
//            System.out.println("Desired replication degree: " + fileInfo.getValue().replicationDegree);
            sb.append("Chunks (").append(fileInfo.getValue().chunks).append(" chunks):\n");

            for (Map.Entry<Integer, ChunkInfo> chunk : chunks.get(fileInfo.getKey()).entrySet()) {
                sb.append("Chunk #").append(chunk.getKey()).append(" (").append(chunk.getValue().peers.size()).append(")\n");
            }

            sb.append("\n");
        }

        sb.append("- Stored chunks:\n");

        for (Map.Entry<String, Map<Integer, ChunkInfo>> fileInfo : chunks.entrySet()) {
            for (Map.Entry<Integer, ChunkInfo> chunkInfo : fileInfo.getValue().entrySet()) {
                if (hasChunk(fileInfo.getKey(), chunkInfo.getKey(), ChordNode.instance().id())) {
                    sb.append("File ").append(fileInfo.getKey()).append(" chunk #").append(chunkInfo.getKey()).append(" ");
//                    System.out.print("(" + chunkInfo.getValue().peers.size() + "/" + chunkInfo.getValue().replicationDegree + ") ");
                    sb.append(chunkInfo.getValue().size).append(" B");
                }

                sb.append("\n");
            }

            sb.append("\n");
        }

        sb.append("Peer's capacity: ")
                .append(Peer.instance().currentDiskSpace.get())
                .append("/")
                .append(Peer.instance().maxDiskSpace.get())
                .append(" B\n");

        return sb.toString();
    }





}
