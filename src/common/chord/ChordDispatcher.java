package common.chord;

import chord.ChordHandler;
import common.Dispatcher;
import utils.Logger;

import javax.net.ssl.SSLSocket;
import java.io.IOException;

/**
 * Chord's dispatcher
 */
public class ChordDispatcher extends Dispatcher {

    /**
     * Creates a new Chord's dispatcher
     * @param port Port
     * @throws IOException
     */
    public ChordDispatcher(Integer port) throws IOException {
        super(port);

        Logger.info("Chord", "started dispatcher at " + server.getLocalPort());
    }

    @Override
    public void awaitConnection() throws IOException {
        ChordConnection connection = new ChordConnection((SSLSocket) server.accept());
        ChordHandler.submit(new ChordWorker(connection));
    }
}