package protocol;

import store.ChunkInfo;
import store.Store;
import utils.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Reclaim protocol
 */
public abstract class Reclaim {

    /** Reclaim instance */
    static AtomicBoolean instance;

    static {
        instance = new AtomicBoolean(false);
    }

    /**
     * Reclaim space
     * @param newSize New max disk space
     */
    public static void reclaimSpace(Integer newSize) {

        if (!ProtocolHandler.isPeerFree()) {
            Logger.warning("Reclaim", "found another protocol instances running");
            return;
        }

        instance.set(true);

        Store.maxDiskSpace.set(newSize);

        if (Store.currentDiskSpace.get() < Store.maxDiskSpace.get()) {
            instance.set(false);
            Logger.fine("Reclaim", "no need to reclaim disk space");
            return;
        }

        boolean stop = false;

        for (Map.Entry<String, Map<Integer, ChunkInfo>> file : Store.chunks.entrySet()) {

            // Order file's chunks by number of replicas (delete chunks with less replicas first)
            List<Map.Entry<Integer, ChunkInfo>> fileChunks = new ArrayList<>(file.getValue().entrySet());
            fileChunks.sort((o1, o2) -> o1.getValue().replicas.size() < o2.getValue().replicas.size() ? -1 : 1);

            if (stop)
                break;

            for (Map.Entry<Integer, ChunkInfo> chunk : fileChunks) {

                Protocol.deleteChunk(file.getKey(), chunk.getKey(), -1);

                Logger.fine("Reclaim", "deleted chunk #" + chunk.getKey() + " from file " + file.getKey());

                if (Store.currentDiskSpace.get() < Store.maxDiskSpace.get()) {
                    stop = true;
                    break;
                }
            }
        }

        instance.set(false);

        Logger.fine("Reclaim", "completed reclaim protocol");
    }
}
