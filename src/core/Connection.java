package core;

import ssl.SocketFactory;

import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;

/**
 * Connection's abstraction
*/
public abstract class Connection {

    /** Connection's socket */
    protected SSLSocket client;
    protected Boolean alive;

    /**
     * Create a new connection
     * @param node Client's socket
     */
    protected Connection(SSLSocket node) {
        client = node;
        alive = true;
    }

    /**
     * Create a new connection
     * @param addr Server's address
     */
    protected Connection(InetSocketAddress addr) {
        try {
            client = SocketFactory.getSocket(addr.getHostName(), addr.getPort());
        } catch (IOException e) {
           // Ignore
        }

        alive = client != null;
    }

    /**
     * Sends a message
     * @param message Message to send
     * @throws IOException
     */
    protected void send(Message message) throws IOException {

        if (client == null)
            throw new IOException();

        ObjectOutputStream os = new ObjectOutputStream(client.getOutputStream());
        os.writeObject(message);
        os.flush();
    }

    /**
     * Receives a message
     * @return Message received
     * @throws IOException
     */
    protected Message receive() throws IOException {

        if (client == null)
            throw new IOException();

        Message message = null;

        try {
            ObjectInputStream is = new ObjectInputStream(client.getInputStream());
            message = (Message) is.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        return message;
    }
}