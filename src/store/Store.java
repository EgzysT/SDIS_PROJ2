package store;

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

//    /**
//     * Register a file that was backed up.
//     * @param fileID            File ID
//     * @param filePath          File path
//     */
    public static void registerFile(String fileID, String filePath, Integer nrChunks) {
        files.putIfAbsent(fileID, new FileInfo(filePath, nrChunks));
    }

//    /**
//     * Register chunk that was stored by a peer.
//     * @param fileID    File ID
//     * @param chunkNo   Chunk number
//     * @param nodeID    Node that stored chunk
//     */
//    public static void registerChunk(String fileID, Integer chunkNo, BigInteger nodeID) {
//        chunks.compute(fileID, (k1, v1) -> {
//            if (v1 == null)
//                v1 = new ConcurrentHashMap<>();
//
//            v1.compute(chunkNo, (k2, v2) -> {
//                if (v2 == null)
//                    v2 = new ChunkInfo();
//
//                v2.peers.add(nodeID);
//
//                return v2;
//            });
//
//            return v1;
//        });
//    }

    /**
     * Register chunk that was stored.
     * @param fileID    File ID
     * @param chunkNo   Chunk number
     */
    public static void registerChunk(String fileID, Integer chunkNo, Integer chunkSize, Integer i) {

        chunks.compute(fileID, (k1, v1) -> {
            if (v1 == null)
                v1 = new ConcurrentHashMap<>();

            v1.compute(chunkNo, (k2, v2) -> {

                if (v2 == null) {
                    v2 = new ChunkInfo(chunkSize);
                    Peer.instance().currentDiskSpace.addAndGet(chunkSize);
                }

                v2.replicas.add(i);

                return v2;
            });

            return v1;
        });
    }

   /**
    * Unregister chunk that was deleted.
    * @param fileID   File ID
    * @param chunkNo  Chunk number
    */
   public static void unregisterChunk(String fileID, Integer chunkNo, Integer i) {

       chunks.computeIfPresent(fileID, (k1, v1) -> {

           v1.computeIfPresent(chunkNo, (k2, v2) -> {

               if (i == -1)
                   v2.replicas.clear();
               else
                   v2.replicas.remove(i);

               if (v2.replicas.isEmpty()) {
                   Peer.instance().currentDiskSpace.addAndGet(-v2.size);
                   v2 = null;
               }

               return v2;
           });

           return v1;
       });
   }

    /**
     * Check if node has chunk backed up.
     * @param fileID File identifier
     * @param chunkNo Chunk number
     * @return True if node has chunk backed up, false otherwise
     */
    public static Boolean hasChunk(String fileID, Integer chunkNo) {
        return chunks.containsKey(fileID)
                && chunks.get(fileID).containsKey(chunkNo);
    }

//    public static Boolean hasChunkReplica(String fileID, Integer chunkNo, Integer i) {
//        return chunks.containsKey(fileID)
//                && chunks.get(fileID).containsKey(chunkNo)
//                && chunks.get(fileID).get(chunkNo).replicas.contains(i);
//    }

    public static String getFile(String filePath) {

        Path fileName = Paths.get(filePath).getFileName();

        for (Map.Entry<String, FileInfo> fileInfo : files.entrySet()) {
            if (Paths.get(fileInfo.getValue().filePath).getFileName().equals(fileName))
                return fileInfo.getKey();
        }

        return null;
    }

//    public static String debug() {
//        StringBuilder sb = new StringBuilder();
//
//        sb.append("- Backed up files:\n");
//
//        for (Map.Entry<String, FileInfo> fileInfo : files.entrySet()) {
//            sb.append("Path ").append(fileInfo.getValue().filePath).append("\n");
//            sb.append("File ").append(fileInfo.getKey()).append("\n");
////            System.out.println("Desired replication degree: " + fileInfo.getValue().replicationDegree);
//            sb.append("Chunks (").append(fileInfo.getValue().chunks).append(" chunks):\n");
//
//            for (Map.Entry<Integer, ChunkInfo> chunk : chunks.get(fileInfo.getKey()).entrySet()) {
//                sb.append("Chunk #").append(chunk.getKey()).append(" (").append(chunk.getValue().peers.size()).append(")\n");
//            }
//
//            sb.append("\n");
//        }
//
//        sb.append("- Stored chunks:\n");
//
//        for (Map.Entry<String, Map<Integer, ChunkInfo>> fileInfo : chunks.entrySet()) {
//            for (Map.Entry<Integer, ChunkInfo> chunkInfo : fileInfo.getValue().entrySet()) {
//                if (hasChunk(fileInfo.getKey(), chunkInfo.getKey(), ChordNode.instance().id())) {
//                    sb.append("File ").append(fileInfo.getKey()).append(" chunk #").append(chunkInfo.getKey()).append(" ");
////                    System.out.print("(" + chunkInfo.getValue().peers.size() + "/" + chunkInfo.getValue().replicationDegree + ") ");
//                    sb.append(chunkInfo.getValue().size).append(" B");
//                }
//
//                sb.append("\n");
//            }
//
//            sb.append("\n");
//        }
//
//        sb.append("Peer's capacity: ")
//                .append(Peer.instance().currentDiskSpace.get())
//                .append("/")
//                .append(Peer.instance().maxDiskSpace.get())
//                .append(" B\n");
//
//        return sb.toString();
//    }





}
