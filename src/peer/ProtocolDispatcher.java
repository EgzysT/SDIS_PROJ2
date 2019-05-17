package peer;

import java.net.ServerSocket;

import core.Dispatcher;

/**
 * ProtocolDispatcher
 */
public class ProtocolDispatcher extends Dispatcher {

    /** Server socket */
	private ServerSocket server;

	/**
     * Creates a new protocol's dispatcher
     * @param port Port
     * @throws IOException
     */
    ProtocolDispatcher(Integer port) throws IOException {
        server = new ServerSocket(port);
    }

    @Override
    protected void awaitConnection() throws IOException {
        ChordConnection connection = new ChordConnection(server.accept());
        executor.submit(new ChordWorker(connection));
    }
}