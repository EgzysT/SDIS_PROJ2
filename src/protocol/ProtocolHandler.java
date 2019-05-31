package protocol;

import chord.ChordNode;
import store.ChunkInfo;
import store.Store;
import utils.Logger;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Protocol handler
 */
public abstract class ProtocolHandler {

    /** Protocol's executor */
    private static ScheduledExecutorService executor;

    static {
        executor = Executors.newScheduledThreadPool(4);
    }

    /**
     * Submits a task
     * @param task task
     */
    public static void submit(Runnable task) {
        executor.submit(task);
    }

    /**
     * Checks if file is being used in any protocol instance.
     * @param fileID fileID
     * @return True if file is busy, false otherwise.
     */
    public static boolean isFileBusy(String fileID) {
        return Backup.instances.containsKey(fileID) || Restore.instances.containsKey(fileID) ||
                Delete.instances.containsKey(fileID) || Reclaim.instance.get();
    }

    /**
     * Checks if peer is running any protocol instance.
     * @return True if is free, false otherwise.
     */
    public static boolean isPeerFree() {
        return Backup.instances.isEmpty() ||
                Restore.instances.isEmpty() || Delete.instances.isEmpty() || !Reclaim.instance.get();
    }

    /**
     * Checks up to 5 chunks to see if file is backed up
     * @param fileID File identifier
     * @return True if file is backed up, false otherwise
     */
    public static boolean isFileBackedUp(String fileID) {

        for (int chunkNo = 0; chunkNo < 5; chunkNo++) {

            byte[] chunk = ChordNode.instance().get(fileID, chunkNo);

            if (chunk != null)
                return true;
        }

        return false;
    }

    public static void syncFiles() {

        System.out.println("syncing");

        for (Map.Entry<String, Map<Integer, ChunkInfo>> file : Store.chunks.entrySet()) {

            if (ProtocolHandler.isFileBackedUp(file.getKey()))
                continue;

            for (Map.Entry<Integer, ChunkInfo> chunk : file.getValue().entrySet()) {
                Protocol.deleteChunk(file.getKey(), chunk.getKey(), -1);
                Logger.fine("Protocol", "deleting old chunk");
            }
        }
    }
}
