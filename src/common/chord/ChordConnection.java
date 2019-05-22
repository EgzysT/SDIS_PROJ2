package common.chord;

import chord.ChordInfo;
import common.Connection;
import utils.Logger;

import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * ChordHandler's connection
 */
public class ChordConnection extends Connection {

    /**
     * Creates a new chord's connection
     * @param socket Client's socket
     */
    public ChordConnection(SSLSocket socket) {
        super(socket);
    }

    /**
     * Creates a new chord's connection
     * @param addr Server's address
     */
    public ChordConnection(InetSocketAddress addr) {
        super(addr);
    }

    /**
     * Requests closest node to given key
     * @param key Identifier to search for
     * @return Closest node to given key
     */
    public ChordInfo findClosest(BigInteger key) {

        if (client == null)
            return null;

        ChordInfo closest = null;

        try {
            send(new ChordMessage(ChordMessage.Type.CLOSEST_PRECEDING_NODE, key));
            closest = ((ChordMessage) receive()).node;
            client.close();
        } catch (IOException e) {
            Logger.warning("ChordHandler", "failed to ask for closest preceding node");
        }

        return closest;
    }

    /**
     * Requests successor to given key
     * @param key Identifier to search for
     * @return Successor node to given key
     */
    public ChordInfo findSuccessor(BigInteger key) {

        if (client == null)
            return null;

        ChordInfo successor = null;

        try {
            send(new ChordMessage(ChordMessage.Type.FIND_SUCCESSOR, key));
            successor = ((ChordMessage) receive()).node;
            client.close();
        } catch (IOException e) {
            Logger.warning("ChordHandler", "failed to ask for finding successor");
        }

        return successor;
    }

    /**
     * Requests node's successor
     * @return Node's successor
     */
    public ChordInfo getSuccessor() {

        if (client == null)
            return null;

        ChordInfo successor = null;

        try {
            send(new ChordMessage(ChordMessage.Type.GET_SUCCESSOR));
            successor = ((ChordMessage) receive()).node;
            client.close();
        } catch (IOException e) {
            Logger.warning("ChordHandler", "failed to ask for successor");
        }

        return successor;
    }

    public List<ChordInfo> getSuccessors() {

        if (client == null)
            return null;

        List<ChordInfo> successors = null;

        try {
            send(new ChordMessage(ChordMessage.Type.GET_SUCCESSORS));
            successors = ((ChordMessage) receive()).nodes;
            client.close();
        } catch (IOException e) {
            Logger.warning("ChordHandler", "failed to ask for successor");
        }

        return successors;
    }

    /**
     * Requests node's predecessor
     * @return Node's predecessor
     */
    public ChordInfo getPredecessor() {

        if (client == null)
            return null;

        ChordInfo predecessor = null;

        try {
            send(new ChordMessage(ChordMessage.Type.GET_PREDECESSOR));
            predecessor = ((ChordMessage) receive()).node;
            client.close();
        } catch (IOException e) {
            Logger.warning("ChordHandler", "failed to ask for predecessor");
        }

        return predecessor;
    }

    /**
     * Notifies node
     * @param node Current node
     */
    public void notify(ChordInfo node) {

        if (client == null)
            return;

        try {
            send(new ChordMessage(ChordMessage.Type.NOTIFY, node));
            client.close();
        } catch (IOException e) {
            Logger.warning("ChordHandler", "failed to notify");
        }
    }

    /**
     * Checks if connection is alive
     * @return True if connection is alive, false otherwise
     */
    public boolean alive() {
        try {
            if (alive) {
                send(new ChordMessage(ChordMessage.Type.ALIVE));
                client.close();
            }
        } catch (IOException e) {
            Logger.warning("ChordHandler", "failed to check connection");
        }

        return alive;
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
            message = (ChordMessage) receive();
        } catch (IOException e) {
            Logger.warning("ChordHandler", "failed to listen to request");
        }

        return message;
    }

    /**
     * Sends a reply to request
     * @param node Node
     */
    void reply(ChordInfo node) {

        if (client == null)
            return;

        try {
            send(new ChordMessage(ChordMessage.Type.NODE, node));
            client.close();
        } catch (IOException e) {
            Logger.warning("ChordHandler", "failed to reply to request");
        }
    }

    void reply(List<ChordInfo> nodes) {

        if (client == null)
            return;

        try {
            send(new ChordMessage(ChordMessage.Type.NODE, nodes));
            client.close();
        } catch (IOException e) {
            Logger.warning("ChordHandler", "failed to reply to request");
        }
    }
}
