package protocol;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
}
