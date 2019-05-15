package chord;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ChordConnection {

    private Socket client;

    ChordConnection(Socket node) {
        client = node;
    }

    ChordConnection(InetSocketAddress node) throws IOException {
        client = new Socket(node.getHostName(), node.getPort());
    }

    void send(ChordMessage message) throws IOException {
        ObjectOutputStream os = new ObjectOutputStream(client.getOutputStream());
        os.writeObject(message);
        os.flush();
    }

    ChordMessage receive() throws IOException {
        try {
            ObjectInputStream is = new ObjectInputStream(client.getInputStream());
            return (ChordMessage) is.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        return null;
    }

    void close() throws IOException {
        client.close();
    }
}