package store;

import utils.Utils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Store {

    public static Map<String, FileInfo> files;
    public static Map<String, Map<Integer, ChunkInfo>> chunks;
    public static AtomicInteger currentDiskSpace, maxDiskSpace;

    static {
        files = new ConcurrentHashMap<>();
        chunks = new ConcurrentHashMap<>();

        currentDiskSpace = new AtomicInteger(0);
        maxDiskSpace = new AtomicInteger(Integer.MAX_VALUE);
    }

    private Store() {}

    /**
     * Registers a file.
     * @param fileID File identifier
     * @param filePath File path
     */
    public static void registerFile(String fileID, String filePath, Integer nrChunks) {
        files.putIfAbsent(fileID, new FileInfo(filePath, nrChunks));
    }

    /**
     * Unregisters a file.
     * @param fileID File identifier
     */
    public static void unregisterFile(String fileID) {
        files.remove(fileID);
    }

    /**
     * Registers a replica of a new chunk
     * @param fileID File identifier
     * @param chunkNo Chunk number
     * @param chunkSize Chunk size
     * @param replicaNo Replica number
     */
    public static void registerChunkReplica(String fileID, Integer chunkNo, Integer chunkSize, Integer replicaNo) {

        chunks.compute(fileID, (k1, v1) -> {

            if (v1 == null)
                v1 = new ConcurrentHashMap<>();

            v1.compute(chunkNo, (k2, v2) -> {

                if (v2 == null) {
                    v2 = new ChunkInfo(chunkSize);
                    currentDiskSpace.addAndGet(chunkSize);
                }

                v2.replicas.add(replicaNo);

                return v2;
            });

            return v1;
        });
    }

    /**
     * Registers a replica of an existing chunk
     * @param fileID File identifier
     * @param chunkNo Chunk number
     * @param replicaNo Replica number
     */
    public static void registerChunkReplica(String fileID, Integer chunkNo, Integer replicaNo) {

        chunks.computeIfPresent(fileID, (k1, v1) -> {

            v1.computeIfPresent(chunkNo, (k2, v2) -> {

                v2.replicas.add(replicaNo);

                return v2;
            });

            return v1;
        });
    }

   /**
    * Unregister replica of an existing chunk
    * @param fileID File identifier
    * @param chunkNo Chunk number
    * @param replicaNo Replica number
    */
   public static void unregisterChunkReplica(String fileID, Integer chunkNo, Integer replicaNo) {

       chunks.computeIfPresent(fileID, (k1, v1) -> {

           v1.computeIfPresent(chunkNo, (k2, v2) -> {

               if (replicaNo == -1)
                   v2.replicas.clear();
               else
                   v2.replicas.remove(replicaNo);

               if (v2.replicas.isEmpty()) {
                   currentDiskSpace.addAndGet(-v2.size);
                   v2 = null;
               }

               return v2;
           });

           if (v1.isEmpty())
               v1 = null;

           return v1;
       });
   }

    /**
     * Checks if node has chunk backed up.
     * @param fileID File identifier
     * @param chunkNo Chunk number
     * @return True if node has chunk backed up, false otherwise
     */
    public static Boolean hasChunk(String fileID, Integer chunkNo) {
        return chunks.containsKey(fileID) && chunks.get(fileID).containsKey(chunkNo);
    }

    public static Boolean hasChunkReplica(String fileID, Integer chunkNo, Integer replicaNo) {
        return hasChunk(fileID, chunkNo) && chunks.get(fileID).get(chunkNo).replicas.contains(replicaNo);
    }

    /**
     * Checks if file is backed up.
     * @param fileID File identifier
     * @return True if file is backed up, false otherwise
     */
    public static Boolean isBackedUp(String fileID) {
        return files.containsKey(fileID) || chunks.containsKey(fileID);
    }

    /**
     * Returns file identifier from file path
     * @param filePath File path
     * @return File identifier corresponding file path
     */
    public static String getFileID(String filePath) {

        Path fileName = Paths.get(filePath).getFileName();

        for (Map.Entry<String, FileInfo> fileInfo : files.entrySet()) {
            if (Paths.get(fileInfo.getValue().filePath).getFileName().equals(fileName))
                return fileInfo.getKey();
        }

        return null;
    }

    public static String debug() {

        Utils.clearScreen();

        StringBuilder sb = new StringBuilder();

        if (files.size() > 0) {
            sb.append("- Backed up files:\n");

            for (Map.Entry<String, FileInfo> fileInfo : files.entrySet()) {

                sb.append("File ")
                        .append(fileInfo.getKey())
                        .append("[").append(fileInfo.getValue().filePath).append("]")
                        .append(" (").append(fileInfo.getValue().chunks).append(" chunks)")
                        .append("\n");
            }

            sb.append("\n");
        }

        if (chunks.size() > 0) {
            sb.append("- Stored chunks:\n");

            for (Map.Entry<String, Map<Integer, ChunkInfo>> fileInfo : chunks.entrySet()) {

                sb.append("File ").append(fileInfo.getKey()).append(":\n");

                for (Map.Entry<Integer, ChunkInfo> chunkInfo : fileInfo.getValue().entrySet()) {

                    sb.append("Chunk #")
                            .append(chunkInfo.getKey())
                            .append(" ")
                            .append(chunkInfo.getValue().size).append(" B")
                            .append(" (").append(Arrays.toString(chunkInfo.getValue().replicas.toArray())).append(")")
                            .append("\n");
                }

                sb.append("\n");
            }
        }

        sb.append("Peer's capacity: ")
                .append(currentDiskSpace.get())
                .append("/")
                .append(maxDiskSpace.get())
                .append(" B\n");

        return sb.toString();
    }
}
