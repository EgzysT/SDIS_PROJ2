package chord;

import core.Dispatcher;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Chord's dispatcher
 */
public class ChordDispatcher extends Dispatcher {

    /** Server socket */
    private ServerSocket server;

    /**
     * Creates a new chord's dispatcher
     * @param port Port
     * @throws IOException
     */
    ChordDispatcher(Integer port) throws IOException {
        server = new ServerSocket(port);
    }

    @Override
    protected void awaitConnection() throws IOException {
        ChordConnection connection = new ChordConnection(server.accept());
        executor.submit(new ChordWorker(connection));
    }
}