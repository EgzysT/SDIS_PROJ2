package protocol;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// TODO protocols should wait for async operations to finish??

/**
 * Protocol handler
 */
public class ProtocolHandler {

    /** Protocol's executor */
    private static ScheduledExecutorService executor;

    static {
        executor = Executors.newScheduledThreadPool(4);
    }

    private ProtocolHandler() {}

    /**
     * Schedules a task using an executor
     * @param task Task
     * @param delay Task's delay (in milliseconds)
     */
    public static void schedule(Runnable task, Integer delay) {
        executor.schedule(
                task,
                delay,
                TimeUnit.MILLISECONDS
        );
    }

    /**
     * Check if file is being used in any protocol instance.
     * @param fileID fileID
     * @return True if file is busy, false otherwise.
     */
    static boolean isFileBusy(String fileID) {
        return Backup.instances.containsKey(fileID) || Restore.instances.containsKey(fileID);
    }

    /**
     * Check if peer is running any protocol instance.
     * @return True if is free, false otherwise.
     */
    static boolean isPeerFree() {
        return Backup.instances.isEmpty() || Restore.instances.isEmpty();
    }


}
