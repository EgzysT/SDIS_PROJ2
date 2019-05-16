package chord;

import core.Connection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

class ChordConnection extends Connection {

    // TODO add error checking like invalid reply or FAIL

    ChordConnection(Socket node) {
        super(node);
    }

    ChordConnection(InetSocketAddress addr)  {
        super(addr);
    }

    NodeInfo findClosest(Integer key) {
        NodeInfo closest = null;

        try {
            send(new ChordMessage(key).type(ChordMessage.MessageType.CLOSEST_PRECEDING_NODE));
            closest = receive().node;
            close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        return closest;
    }

    NodeInfo findSuccessor(Integer key) {
        NodeInfo successor = null;

        try {
            send(new ChordMessage(key).type(ChordMessage.MessageType.FIND_SUCCESSOR));
            successor = receive().node;
            close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        return successor;
    }

    NodeInfo getSuccessor() {
        NodeInfo successor = null;

        try {
            send(new ChordMessage().type(ChordMessage.MessageType.GET_SUCCESSOR));
            successor = receive().node;
            close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        return successor;
    }

    NodeInfo getPredecessor() {
        NodeInfo predecessor = null;

        try {
            send(new ChordMessage().type(ChordMessage.MessageType.GET_PREDECESSOR));
            predecessor = receive().node;
            close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        return predecessor;
    }

    void notify(NodeInfo node) {
        try {
            send(new ChordMessage(node).type(ChordMessage.MessageType.NOTIFY));
            close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    void debug() {
        try {
            send(new ChordMessage().type(ChordMessage.MessageType.DEBUG));
            close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    ChordMessage listen() {
        ChordMessage message = null;

        try {
            message = receive();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        return message;
    }

    void reply(NodeInfo node) {

        try {
            send(new ChordMessage(node).type(ChordMessage.MessageType.NODE));
            close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
