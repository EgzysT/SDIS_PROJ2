package chord;

import utils.Logger;
import utils.Utils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * Node's info
 */
class NodeInfo implements Serializable {

    /** Identifier */
    Integer identifier;

    /** Address */
    InetSocketAddress address;

    /**
     * Creates a new node's info
     * @param id Identifier
     * @param addr Address
     */
    NodeInfo(Integer id, InetSocketAddress addr) {
        identifier = id;
        address = addr;
    }

    @Override
    public String toString() {
        return identifier + " - " + address;
    }
}

/**
 * Chord's node
 */
public final class ChordNode {

    // TODO add stuff for fault tolerance

    /** Singleton instance */
    private static ChordNode instance;

    /** Node's info */
    private static NodeInfo info;

    /** Finger table */
    private static NodeInfo[] finger_table;

    /** Predecessor */
    private static NodeInfo predecessor;

    /**
     * Constructor
     */
    private ChordNode() {}

    /**
     * Creates a new node's instance, if necessary, and returns it
     * @return Node's instance
     */
    static ChordNode instance() {

        if (instance == null)
            instance = new ChordNode();

        return instance;
    }

    /**
     * Initializes node's properties
     * @param addr Address
     */
    private void init(InetSocketAddress addr) {

        info = new NodeInfo(
                Utils.hash(addr.toString()).mod(new BigInteger("2").pow(Chord.m)).intValue(),
                addr
        );

        finger_table = new NodeInfo[Chord.m];
    }

    /**
     * Initializes node's threads
     */
    private void initThreads() {

        try {
            // Dispatcher
            new Thread(
                    new ChordDispatcher(info.address.getPort())
            ).start();

            // Stabilize
            Chord.executor.schedule(
                    this::stabilize,
                    50,
                    TimeUnit.MILLISECONDS
            );

            // Fix finger
            Chord.executor.schedule(
                    () -> fixFinger(0),
                    100,
                    TimeUnit.MILLISECONDS
            );

            // Check predecessor
            Chord.executor.schedule(
                    this::checkPredecessor,
                    150,
                    TimeUnit.MILLISECONDS
            );

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * Initializes supernode
     */
    void initSuperNode() {

        init(Chord.supernode);

        predecessor(info);
        successor(info);

        initThreads();

        Logger.info("Chord", "starting super node " + info.identifier + " at " + info.address);
    }

    /**
     * Initializes node
     * @param addr Address
     */
    void initNode(InetSocketAddress addr) {

        init(addr);

        // Check if supernode is alive
        if (!new ChordConnection(Chord.supernode).alive()) {
            Logger.info("Chord", "supernode is not alive");
            return;
        }

        predecessor(null);
        successor(new ChordConnection(Chord.supernode).findSuccessor(info.identifier));

        initThreads();

        Logger.info("Chord", "starting node " + info.identifier + " at " + info.address);
    }

    /**
     * Finds successor of given key
     * @param key Identifier
     * @return Successor of given key
     */
    NodeInfo findSuccessor(Integer key) {

        NodeInfo node = info;
        NodeInfo successor = successor();

        while (!Utils.in_range(key, node.identifier, successor.identifier, true)) {

            if (node.equals(info))
                node = closestPrecedingNode(key);
            else
                node = new ChordConnection(node.address).findClosest(key);

            if (node.equals(info))
                successor = successor();
            else
                successor = new ChordConnection(node.address).getSuccessor();
        }

        return successor;
    }

    /**
     * Finds closest preceding ndoe of given key in the finger table
     * @param key Identifier
     * @return Closest preceding node of given key
     */
    NodeInfo closestPrecedingNode(Integer key) {

        for (int i = Chord.m - 1; i >= 0; i--) {

            if (finger_table[i] == null)
                continue;

            if (Utils.in_range(finger_table[i].identifier, info.identifier, key, false)) {

                if (!new ChordConnection(finger_table[i].address).alive()) {
                    finger_table[i] = info;
                    Logger.fine("Chord", "finger not alive, updating");
                } else {
                    return finger_table[i];
                }
            }
        }

        return info;
    }

    /**
     * Checks if predecessor is alive
     */
    private void checkPredecessor() {

        if (predecessor() != null && !predecessor().equals(info) && !new ChordConnection(predecessor().address).alive()) {
            if (Chord.supernode.equals(info.address))
                predecessor = info;
            else
                predecessor(null);

            Logger.fine("Chord", "predecessor not alive, updating");
        }

        Chord.executor.schedule(
            this::checkPredecessor,
            2,
            TimeUnit.SECONDS
        );
    }

    /**
     * Stabilizes node and notifies successor of node's existence
     */
    private void stabilize() {

        NodeInfo x;

        if (successor().equals(info))
            x = predecessor();
        else
            x = new ChordConnection(successor().address).getPredecessor();

        if (x == null) {
            if (Chord.supernode.equals(info.address))
                successor(findSuccessor(info.identifier));
            else
                successor(new ChordConnection(Chord.supernode).findSuccessor(info.identifier));

            // If supernode is offline
            if (successor() == null)
                successor(info);

            Logger.fine("Chord", "successor not alive, updating");
        } else {
            if (Utils.in_range(x.identifier, info.identifier, successor().identifier, false)) {
                successor(x);
                Logger.fine("Chord", "updated successor");
            }

            if (successor().equals(info))
                notify(info);
            else
                new ChordConnection(successor().address).notify(info);
        }

        Chord.executor.schedule(
                this::stabilize,
                2,
                TimeUnit.SECONDS
        );
    }

    /**
     * Checks if given node is predecessor
     * @param node Possible predecessor node
     */
    void notify(NodeInfo node) {

        if (predecessor == null || Utils.in_range(node.identifier, predecessor.identifier, info.identifier, false)) {
            predecessor(node);
            Logger.fine("Chord", "updated predecessor");
        }
    }

    /**
     * Fixes finger i of node's finger table
     * @param i Finger's number
     */
    private void fixFinger(Integer i) {

        finger_table[i] = findSuccessor(Utils.start(info.identifier, i + 1));

        Logger.fine("Chord", "updated finger " + (i + 1));

        Chord.executor.schedule(
                () -> fixFinger((i + 1) % Chord.m),
                2,
                TimeUnit.SECONDS
        );
    }

    /**
     * Get node's successor
     * @return Node's successor
     */
    NodeInfo successor() {
        return finger_table[0];
    }

    /**
     * Get node's predecessor
     * @return Node's predecessor
     */
    NodeInfo predecessor() {
        return predecessor;
    }

    /**
     * Set node's successor
     * @param node New node's successor
     */
    private void successor(NodeInfo node) {
        finger_table[0] = node;
    }

    /**
     * Set node's predecessor
     * @param node New node's predecessor
     */
    private void predecessor(NodeInfo node) {
        predecessor = node;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Node ")
                .append(info.identifier)
                .append(" at ")
                .append(info.address)
                .append("\nPredecessor: ")
                .append(predecessor)
                .append('\n');

        for (int i = 0; i < Chord.m; i++) {
            sb.append("Finger #")
                    .append(i + 1)
                    .append(": ")
                    .append(finger_table[i])
                    .append('\n');
        }

        return sb.toString();
    }

    public static void main(String[] args) {

        String sslDir = "C:\\Users\\Miguel Teixeira\\Desktop\\SDIS_PROJ2\\src\\ssl\\";

        System.setProperty("javax.net.ssl.keyStore", sslDir + "common.keys");
        System.setProperty("javax.net.ssl.keyStorePassword", "123456");

        System.setProperty("javax.net.ssl.trustStore", sslDir + "truststore");
        System.setProperty("javax.net.ssl.trustStorePassword", "123456");


        if (args[0].equals("SUPER")) {
            ChordNode.instance().initSuperNode();
        } else if (args[0].equals("DEBUG")) {
            new ChordConnection(new InetSocketAddress(args[1], Integer.parseInt(args[2]))).debug();
        } else {
            ChordNode.instance().initNode(new InetSocketAddress(args[0], Integer.parseInt(args[1])));
        }
    }
}