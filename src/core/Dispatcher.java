package core;

import utils.Logger;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Dispatcher's abstraction
 */
public abstract class Dispatcher implements Runnable {

    /** Dispatcher's executor, shared with all dispatchers */
    protected static ThreadPoolExecutor executor;

    static {
        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                awaitConnection();
            }
        } catch (IOException e) {
            Logger.severe("Dispatcher", "error in dispatcher");
        }
    }

    /**
     * Handles incoming connection
     * @throws IOException
     */
    protected abstract void awaitConnection() throws IOException;
}
