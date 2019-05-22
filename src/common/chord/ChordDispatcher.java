package common.chord;

import chord.ChordNode;
import common.Dispatcher;
import utils.Logger;

import javax.net.ssl.SSLSocket;
import java.io.IOException;

/**
 * ChordHandler's dispatcher
 */
public class ChordDispatcher extends Dispatcher {

    /**
     * Creates a new chord's dispatcher
     * @param port Port
     * @throws IOException
     */
    public ChordDispatcher(Integer port) throws IOException {
        super(port);

        Logger.info("Dispatcher", "started chord dispatcher at " + server.getLocalPort());
    }

    @Override
    public void awaitConnection() throws IOException {
        ChordConnection connection = new ChordConnection((SSLSocket) server.accept());
        executor.submit(new ChordWorker(connection));
    }
}