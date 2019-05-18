package core;

import ssl.SSLSocket;

import java.io.IOException;;
import java.net.InetSocketAddress;

/**
 * Connection's abstraction
*/
public abstract class Connection {

    /** Connection' socket */
    protected SSLSocket client;

    /**
     * Create a new connection
     * @param node Client's socket
     */
    protected Connection(SSLSocket node) {
        client = node;
    }

    /**
     * Create a new connection
     * @param addr Server's address
     */
    protected Connection(InetSocketAddress addr) {
        try {
            client = new SSLSocket(addr.getHostName(), addr.getPort());
        } catch (IOException e) {
           // Ignore
        }
    }
}