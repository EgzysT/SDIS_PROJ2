package core;

import chord.ChordMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public abstract class Connection {

    // TODO change to ssl socket later
    protected Socket client;

    protected Connection(Socket node) {
        client = node;
    }

    protected Connection(InetSocketAddress addr) {
        try {
            client = new Socket(addr.getHostName(), addr.getPort());
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    protected void send(ChordMessage message) throws IOException {
        ObjectOutputStream os = new ObjectOutputStream(client.getOutputStream());
        os.writeObject(message);
        os.flush();
    }

    protected ChordMessage receive() throws IOException {
        ChordMessage message = null;

        try {
            ObjectInputStream is = new ObjectInputStream(client.getInputStream());
            message = (ChordMessage) is.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        return message;
    }

    protected void close() throws IOException {
        client.close();
    }
}