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

    /** Server socket */
    protected SSLServerSocket server;

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
     * Handles incoming connection
     * @throws IOException
     */
    protected abstract void awaitConnection() throws IOException;
}
