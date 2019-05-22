package common.protocol;

import chord.ChordNode;
import common.Dispatcher;
import utils.Logger;

import javax.net.ssl.SSLSocket;
import java.io.IOException;

/**
 * Protocol dispatcher
 */
public class ProtocolDispatcher extends Dispatcher {

    /**
     * Creates a new protocol's dispatcher
     * @param port Port
     * @throws IOException
     */
    public ProtocolDispatcher(Integer port) throws IOException {
        super(port);

        ChordNode.instance().info.setProtocolPort(server.getLocalPort());

        Logger.info("Dispatcher", "started protocol dispatcher at " + server.getLocalPort());
    }

    @Override
    public void awaitConnection() throws IOException {
        ProtocolConnection connection = new ProtocolConnection((SSLSocket) server.accept());
        executor.submit(new ProtocolWorker(connection));
    }
}

