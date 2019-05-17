package core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Connection's abstraction
*/
public abstract class Connection {

//     TODO change to ssl ocket later

    /** Connection' socket */
    protected Socket client;

    /**
     * Create a new connection
     * @param node Client's socket
     */
    protected Connection(Socket node) {
        client = node;
    }

    /**
     * Create a new connection
     * @param addr Server's address
     */
    protected Connection(InetSocketAddress addr) {
        try {
            client = new Socket(addr.getHostName(), addr.getPort());
        } catch (IOException e) {
            // Ignore
        }
    }

    /**
     * Sends a message
     * @param message Message to send
     * @throws IOException
     */
    protected void send(Message message) throws IOException {
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

    /**
     * Closes connection
     * @throws IOException
     */
    protected void close() throws IOException {
        client.close();
    }
}