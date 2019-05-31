package store;

import chord.ChordHandler;
import peer.Peer;
import utils.Logger;
import utils.Utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Store
 */
public abstract class Store {

    /** Chunks backed up by this node */
    public static Map<String, Map<Integer, ChunkInfo>> chunks;

    /**
     * Current disk space and max disk space
     */
    public static AtomicInteger currentDiskSpace, maxDiskSpace;

    static {
        chunks = new ConcurrentHashMap<>();
        currentDiskSpace = new AtomicInteger(0);
        maxDiskSpace = new AtomicInteger(Integer.MAX_VALUE);
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
    * Unregisters replica of an existing chunk
    * @param fileID File identifier
    * @param chunkNo Chunk number
    * @param replicaNo Replica number (-1 for all replicas)
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
     * Checks if node has chunk backed up
     * @param fileID File identifier
     * @param chunkNo Chunk number
     * @return True if node has chunk backed up, false otherwise
     */
    public static Boolean hasChunk(String fileID, Integer chunkNo) {
        return chunks.containsKey(fileID) && chunks.get(fileID).containsKey(chunkNo);
    }

    /**
     * Checks if node has chunk's replica
     * @param fileID File identifier
     * @param chunkNo Chunk number
     * @param replicaNo Replica number
     * @return True if node has chunk's replica, false otherwise
     */
    public static Boolean hasChunkReplica(String fileID, Integer chunkNo, Integer replicaNo) {
        return hasChunk(fileID, chunkNo) && chunks.get(fileID).get(chunkNo).replicas.contains(replicaNo);
    }

    /**
     * Imports store, if available
     */
    @SuppressWarnings("unchecked")
    public static void importStore() {

        try {

            if (Files.exists(Paths.get(Peer.instance().homeDir + File.separator + "store.bak"))) {

                FileInputStream file = new FileInputStream(
                        Peer.instance().homeDir + File.separator + "store.bak"
                );

                ObjectInputStream in = new ObjectInputStream(file);


                HashMap<String, Object> objects = (HashMap<String, Object>) in.readObject();

                chunks = (Map<String, Map<Integer, ChunkInfo>>) objects.get("chunks");
                currentDiskSpace = (AtomicInteger) objects.get("currentDiskSpace");
                maxDiskSpace = (AtomicInteger) objects.get("maxDiskSpace");

                in.close();
                file.close();

                Logger.fine("Chord", "imported store");
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        ChordHandler.schedule(
                Store::exportStore,
                5000
        );
    }

    /**
     * Exports store, if available
     */
    public static void exportStore() {

        try {
            FileOutputStream file = new FileOutputStream(
                    Peer.instance().homeDir + File.separator + "store.bak"
            );

            ObjectOutputStream out = new ObjectOutputStream(file);

            HashMap<String, Object> objects = new HashMap<>();

            objects.put("chunks", Store.chunks);
            objects.put("currentDiskSpace", Store.currentDiskSpace);
            objects.put("maxDiskSpace", Store.maxDiskSpace);

            out.writeObject(objects);
            out.flush();

            out.close();
            file.close();

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        Logger.fine("Chord", "exported store");

        ChordHandler.schedule(
                Store::exportStore,
                5000
        );
    }

    public static String debug() {

        Utils.clearScreen();

        StringBuilder sb = new StringBuilder();

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