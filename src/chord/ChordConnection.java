package chord;

import core.Connection;
import utils.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

class ChordConnection extends Connection {

    // TODO add error checking like invalid reply or FAIL

    ChordConnection(Socket node) {
        super(node);
    }

    ChordConnection(InetSocketAddress addr) {
        super(addr);
    }

    NodeInfo findClosest(Integer key) {

        if (client == null)
            return null;

        NodeInfo closest = null;

        try {
            send(new ChordMessage(key).type(ChordMessage.MessageType.CLOSEST_PRECEDING_NODE));
            closest = ((ChordMessage) receive()).node;
            close();
        } catch (IOException e) {
            Logger.warning("Chord", "failed to ask for closest preceding node");
        }

        return closest;
    }

    NodeInfo findSuccessor(Integer key) {

        if (client == null)
            return null;

        NodeInfo successor = null;

        try {
            send(new ChordMessage(key).type(ChordMessage.MessageType.FIND_SUCCESSOR));
            successor = ((ChordMessage) receive()).node;
            close();
        } catch (IOException e) {
            Logger.warning("Chord", "failed to ask for finding successor");
        }

        return successor;
    }

    NodeInfo getSuccessor() {

        if (client == null)
            return null;

        NodeInfo successor = null;

        try {
            send(new ChordMessage().type(ChordMessage.MessageType.GET_SUCCESSOR));
            successor = ((ChordMessage) receive()).node;
            close();
        } catch (IOException e) {
            Logger.warning("Chord", "failed to ask for successor");
        }

        return successor;
    }

    NodeInfo getPredecessor() {

        if (client == null)
            return null;

        NodeInfo predecessor = null;

        try {
            send(new ChordMessage().type(ChordMessage.MessageType.GET_PREDECESSOR));
            predecessor = ((ChordMessage) receive()).node;
            close();
        } catch (IOException e) {
            Logger.warning("Chord", "failed to ask for predecessor");
        }

        return predecessor;
    }

    void notify(NodeInfo node) {

        if (client == null)
            return;

        try {
            send(new ChordMessage(node).type(ChordMessage.MessageType.NOTIFY));
            close();
        } catch (IOException e) {
            Logger.warning("Chord", "failed to notify");
        }
    }

    void debug() {

        if (client == null)
            return;

        try {
            send(new ChordMessage().type(ChordMessage.MessageType.DEBUG));
            close();
        } catch (IOException e) {
            Logger.warning("Chord", "failed to notify");
        }
    }

    ChordMessage listen() {

        if (client == null)
            return null;

        ChordMessage message = null;

        try {
            message = (ChordMessage) receive();
        } catch (IOException e) {
            Logger.warning("Chord", "failed to listen to request");
        }

        return message;
    }

    void reply(NodeInfo node) {

        if (client == null)
            return;

        try {
            send(new ChordMessage(node).type(ChordMessage.MessageType.NODE));
            close();
        } catch (IOException e) {
            Logger.warning("Chord", "failed to reply to request");
        }
    }

    boolean alive() {
        boolean alive = client != null;

        try {
            if (alive) {
                send(new ChordMessage().type(ChordMessage.MessageType.ALIVE));
                close();
            }
        } catch (IOException e) {
            Logger.warning("Chord", "failed to check connection");
        }

        return alive;
    }
}
