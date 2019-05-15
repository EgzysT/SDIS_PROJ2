package core;

import utils.Logger;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public abstract class Dispatcher implements Runnable {

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

    protected abstract void awaitConnection() throws IOException;
}
