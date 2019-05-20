package chord;

import utils.Logger;
import utils.Utils;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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

    /** Singleton instance */
    private static ChordNode instance;

    /** Node's info */
    private NodeInfo info;

    /** Finger table */
    private Map<Integer, NodeInfo> finger_table;

    /** Successors */
    private Map<Integer, NodeInfo> successors;

    /** Predecessor */
    private NodeInfo predecessor;

    /**
     * Constructor
     */
    private ChordNode() {}

    /**
     * Creates a new node's instance, if necessary, and returns it
     * @return Node's instance
     */
    public static ChordNode instance() {

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

        finger_table = new ConcurrentHashMap<>();

        successors = new ConcurrentHashMap<Integer, NodeInfo>() {{
            for (int i = 1; i <= Chord.r; i++) {
                put(i, info);
            }
        }};

        predecessor = null;
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
                    () -> fixFinger(1),
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
     * Initializes super node
     */
    public void initSuperNode() {

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
    public void initNode(InetSocketAddress addr) {

        init(addr);

        if (!new ChordConnection(Chord.supernode).alive()) {
            Logger.info("Chord", "super node is not alive");
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
     * Finds closest preceding node of given key in the finger table
     * @param key Identifier
     * @return Closest preceding node of given key
     */
    NodeInfo closestPrecedingNode(Integer key) {

        for (int i = Chord.m; i > 0; i--) {

            if (finger_table.get(i) == null)
                continue;

            if (Utils.in_range(finger_table.get(i).identifier, info.identifier, key, false)) {

                if (!new ChordConnection(finger_table.get(i).address).alive())
                    clearNode(finger_table.get(i));
                else
                    return finger_table.get(i);
            }
        }

        return info;
    }

    /**
     * Checks if predecessor is alive
     */
    private void checkPredecessor() {

        if (predecessor() != null && !predecessor().equals(info) && !new ChordConnection(predecessor().address).alive()) {

            clearNode(predecessor);

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

        NodeInfo node = info;

        for (int i = 1; i <= Chord.r; i++){

            NodeInfo p;

            if (successors.get(i).equals(info))
                p = predecessor();
            else
                p = new ChordConnection(successors.get(i).address).getPredecessor();

            if (p == null) {
                if (Chord.supernode.equals(info.address))
                    successors.put(i, findSuccessor(node.identifier));
                else
                    successors.put(i, new ChordConnection(Chord.supernode).findSuccessor(node.identifier));
            } else if (Utils.in_range(p.identifier, node.identifier, successors.get(i).identifier, false)) {
                successors.put(i, p);
            }

            node = successors.get(i);
        }

        System.out.println(ChordNode.instance());

        if (successor().equals(info))
            notify(info);
        else
            new ChordConnection(successor().address).notify(info);

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

        Integer start = info.identifier + (int) Math.pow(2, i - 1);
        finger_table.put(i, findSuccessor(start));

        Logger.fine("Chord", "updated finger " + i);

        Chord.executor.schedule(
                () -> fixFinger(i % Chord.m + 1),
                2,
                TimeUnit.SECONDS
        );
    }

    private void clearNode(NodeInfo node) {

        for (Map.Entry<Integer, NodeInfo> finger : finger_table.entrySet()) {
            if (finger.getValue().equals(node))
                finger_table.remove(finger.getKey());
        }

        for (Map.Entry<Integer, NodeInfo> successor : successors.entrySet()) {
            if (successor.getValue().equals(node))
                successor.setValue(info);
        }
    }

    /**
     * Get node's successor
     * @return Node's successor
     */
    NodeInfo successor() {
        for (NodeInfo successor : successors.values()) {
            if (!new ChordConnection(successor.address).alive())
                clearNode(successor);
            else
                return successor;
        }

        return info;
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
        finger_table.put(1, node);
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

        Utils.clearScreen();

        StringBuilder sb = new StringBuilder();

        sb.append("Node ")
                .append(info.identifier)
                .append(" at ")
                .append(info.address)
                .append("\nPredecessor: ")
                .append(predecessor)
                .append('\n');

        for (int i = 1; i <= Chord.m; i++) {
            sb.append("Finger #")
                    .append(i)
                    .append(": ")
                    .append(finger_table.get(i))
                    .append('\n');
        }

        for (int i = 1; i <= Chord.r; i++) {
            sb.append("Successor #")
                    .append(i)
                    .append(": ")
                    .append(successors.get(i))
                    .append('\n');
        }

        return sb.toString();
    }

    public static void main(String[] args) {
        String sslDir = "/home/miguelalexbt/IdeaProjects/SDIS_PROJ2/src/ssl/";

        System.setProperty("javax.net.ssl.keyStore", sslDir + "keystore.keys");
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