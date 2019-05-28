package common;

import ssl.SocketFactory;
import utils.Logger;

import javax.net.ssl.SSLServerSocket;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Dispatcher's abstraction
 */
public abstract class Dispatcher implements Runnable {

    /** Dispatcher's executor, shared with all dispatchers */
    protected static ThreadPoolExecutor executor;

    /** Server socket */
    protected SSLServerSocket server;

    static {
        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(6);
    }

    public Dispatcher(Integer port) throws IOException {
        server = SocketFactory.getServerSocket(port);
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
     * Handles incoming core
     * @throws IOException
     */
    protected abstract void awaitConnection() throws IOException;
}
