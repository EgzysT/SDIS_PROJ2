package chord;

import core.Connection;
import core.Dispatcher;

import java.io.IOException;
import java.net.ServerSocket;

public class ChordDispatcher extends Dispatcher {

    private ServerSocket server;

    ChordDispatcher(Integer port) throws IOException {
        server = new ServerSocket(port);
    }

    @Override
    protected void awaitConnection() throws IOException {
        ChordConnection connection = new ChordConnection(server.accept());
        executor.submit(new ChordWorker(connection));
    }
}