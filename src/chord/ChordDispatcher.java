package chord;

import core.Dispatcher;
import ssl.SocketFactory;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import java.io.IOException;

/**
 * Chord's dispatcher
 */
public class ChordDispatcher extends Dispatcher {

    /** Server socket */
    private SSLServerSocket server;

    /**
     * Creates a new chord's dispatcher
     * @param port Port
     * @throws IOException
     */
    ChordDispatcher(Integer port) throws IOException {
        server = SocketFactory.getServerSocket(port);
    }

    @Override
    protected void awaitConnection() throws IOException {
        ChordConnection connection = new ChordConnection((SSLSocket) server.accept());
        executor.submit(new ChordWorker(connection));
    }
}