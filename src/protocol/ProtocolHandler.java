package protocol;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ProtocolHandler {

    /** Protocol's executor */
    private static ScheduledExecutorService executor;

    static {
        executor = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
    }

    /**
     * Schedules a task using an executor
     * @param task Task
     * @param delay Task's delay (in milisseconds)
     */
    public static void schedule(Runnable task, Integer delay) {
        executor.schedule(
                task,
                delay,
                TimeUnit.MILLISECONDS
        );
    }

    private ProtocolHandler() {}
}
