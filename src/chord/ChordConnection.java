package chord;

import core.Connection;
import ssl.SSLSocket;
import utils.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Chord' connection
 */
class ChordConnection extends Connection {

    /**
     * Creates a new chord's connection
     * @param node Client's socket
     */
    ChordConnection(SSLSocket node) {
        super(node);
    }

    /**
     * Creates a new chord's connection
     * @param addr Server's address
     */
    ChordConnection(InetSocketAddress addr) {
        super(addr);
    }

    /**
     * Requests closest node to given key
     * @param key Identifier to search for
     * @return Closest node to given key
     */
    NodeInfo findClosest(Integer key) {

        if (client == null)
            return null;

        NodeInfo closest = null;

        try {
            client.send(new ChordMessage(key).type(ChordMessage.MessageType.CLOSEST_PRECEDING_NODE));
            closest = ((ChordMessage) client.receive()).node;
            client.close();
        } catch (IOException e) {
            Logger.warning("Chord", "failed to ask for closest preceding node");
        }

        return closest;
    }

    /**
     * Requests successor to given key
     * @param key Identifier to search for
     * @return Successor node to given key
     */
    NodeInfo findSuccessor(Integer key) {

        if (client == null)
            return null;

        NodeInfo successor = null;

        try {
            client.send(new ChordMessage(key).type(ChordMessage.MessageType.FIND_SUCCESSOR));
            successor = ((ChordMessage) client.receive()).node;
            client.close();
        } catch (IOException e) {
            Logger.warning("Chord", "failed to ask for finding successor");
        }

        return successor;
    }

    /**
     * Requests node's successor
     * @return Node's successor
     */
    NodeInfo getSuccessor() {

        if (client == null)
            return null;

        NodeInfo successor = null;

        try {
            client.send(new ChordMessage().type(ChordMessage.MessageType.GET_SUCCESSOR));
            successor = ((ChordMessage) client.receive()).node;
            client.close();
        } catch (IOException e) {
            Logger.warning("Chord", "failed to ask for successor");
        }

        return successor;
    }

    /**
     * Requests node's predecessor
     * @return Node's predecessor
     */
    NodeInfo getPredecessor() {

        if (client == null)
            return null;

        NodeInfo predecessor = null;

        try {
            client.send(new ChordMessage().type(ChordMessage.MessageType.GET_PREDECESSOR));
            predecessor = ((ChordMessage) client.receive()).node;
            client.close();
        } catch (IOException e) {
            Logger.warning("Chord", "failed to ask for predecessor");
        }

        return predecessor;
    }

    /**
     * Notifies node
     * @param node Current node
     */
    void notify(NodeInfo node) {

        if (client == null)
            return;

        try {
            client.send(new ChordMessage(node).type(ChordMessage.MessageType.NOTIFY));
            client.close();
        } catch (IOException e) {
            Logger.warning("Chord", "failed to notify");
        }
    }

    /**
     * Ask for node's state
     */
    void debug() {

        if (client == null)
            return;

        try {
            client.send(new ChordMessage().type(ChordMessage.MessageType.DEBUG));
            client.close();
        } catch (IOException e) {
            Logger.warning("Chord", "failed to notify");
        }
    }

    /**
     * Listen to requests
     * @return Request received
     */
    ChordMessage listen() {

        if (client == null)
            return null;

        ChordMessage message = null;

        try {
            message = (ChordMessage) client.receive();
        } catch (IOException e) {
            Logger.warning("Chord", "failed to listen to request");
        }

        return message;
    }

    /**
     * Sends a reply to request
     * @param node
     */
    void reply(NodeInfo node) {

        if (client == null)
            return;

        try {
            client.send(new ChordMessage(node).type(ChordMessage.MessageType.NODE));
            client.close();
        } catch (IOException e) {
            Logger.warning("Chord", "failed to reply to request");
        }
    }

    /**
     * Checks if connection is alive
     * @return True if connection is alive, false otherwise
     */
    boolean alive() {
        boolean alive = client != null;

        try {
            if (alive) {
                client.send(new ChordMessage().type(ChordMessage.MessageType.ALIVE));
                client.close();
            }
        } catch (IOException e) {
            Logger.warning("Chord", "failed to check connection");
        }

        return alive;
    }
}
