package protocol;

import store.ChunkInfo;
import store.Store;
import utils.Logger;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class Reclaim {

    static AtomicBoolean instance;

    static {
        instance = new AtomicBoolean(false);
    }

    public static void reclaimSpace(Integer newSize) {

        if (!ProtocolHandler.isPeerFree()) {
            Logger.warning("Reclaim", "found another protocol instances running");
            return;
        }

        instance.set(true);

        Store.maxDiskSpace.set(newSize);

        if (Store.currentDiskSpace.get() < Store.maxDiskSpace.get()) {
            Logger.fine("Reclaim", "no need to reclaim disk space");
            return;
        }

        boolean stop = false;

        for (Map.Entry<String, Map<Integer, ChunkInfo>> file : Store.chunks.entrySet()) {

            if (stop)
                break;

            for (Map.Entry<Integer, ChunkInfo> chunk : file.getValue().entrySet()) {

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
