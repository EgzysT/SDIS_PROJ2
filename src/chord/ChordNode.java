package chord;

import core.Connection;
import utils.Logger;
import utils.Utils;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

class NodeInfo implements Serializable {
    Integer identifier;
    InetSocketAddress address;

    NodeInfo(Integer id, InetSocketAddress addr) {
        identifier = id;
        address = addr;
    }

    @Override
    public String toString() {
        return identifier + " - " + address;
    }
}

public final class ChordNode {

    // TODO add stuff for fault tolerance
    // TODO add stuff to leave chord ring

    private static ChordNode instance;
    private static NodeInfo info;
    private static NodeInfo[] finger_table;
    private static NodeInfo predecessor;

    private ChordNode() {}

    static ChordNode instance() {
        if (instance == null)
            instance = new ChordNode();
        return instance;
    }

    private void init(InetSocketAddress addr) {

        info = new NodeInfo(
                Utils.hash(addr.toString()).mod(new BigInteger("2").pow(Chord.m)).intValue(),
                addr
        );

        finger_table = new NodeInfo[Chord.m];
    }

    private void initThreads() {

        try {
            // Dispatcher
            new Thread(new ChordDispatcher(info.address.getPort())).start();

            Chord.executor.execute(this::stabilize);
            Chord.executor.execute(() -> fixFinger(0));

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    void initSuperNode() {
        init(Chord.supernode);

        predecessor = info;

        for (int i = 0; i < Chord.m; i++) {
            finger_table[i] = info;
        }

        initThreads();

        Logger.info("Chord", "starting super node " + info.identifier + " at " + info.address);
    }

    void initNode(InetSocketAddress addr) {
        init(addr);

        //TODO check for null (connection ignores it)
        predecessor = null;
        successor(new ChordConnection(Chord.supernode).findSuccessor(info.identifier));

        initThreads();

        Logger.info("Chord", "starting node " + info.identifier + " at " + info.address);
    }

    NodeInfo successor() {
        return finger_table[0];
    }

    NodeInfo predecessor() {
        return predecessor;
    }

    void successor(NodeInfo info) {
        finger_table[0] = info;
    }

    void predecessor(NodeInfo info) {
        predecessor = info;
    }

    NodeInfo findSuccessor(Integer key) {

        NodeInfo node = info;
        NodeInfo successor = successor();

        // TODO if node is self, dont waste a socket and just call function
        // TODO check if successor exists??
        while (!Utils.in_range(key, node.identifier, successor.identifier, true)) {
            node = new ChordConnection(node.address).findClosest(key);
            successor = new ChordConnection(node.address).getSuccessor();
        }

        return successor;
    }

    NodeInfo closestPrecedingNode(Integer key) {

        for (int i = Chord.m - 1; i >= 0; i--) {
            NodeInfo finger = finger_table[i];

            // Ignore if not fingered yet
            if (finger == null)
                continue;

            // TODO check if alive, remove if not
            if (Utils.in_range(finger.identifier, info.identifier, key, false))
                return finger;
        }

        return info;
    }

    private void stabilize() {

        NodeInfo x = new ChordConnection(successor().address).getPredecessor();

        if (Utils.in_range(x.identifier, info.identifier, successor().identifier, false))
            successor(x);

        new ChordConnection(successor().address).notify(info);

        Chord.executor.schedule(
                this::stabilize,
                500,
                TimeUnit.MILLISECONDS
        );
    }

    void notify(NodeInfo node) {

        // Check if node should be predecessor
        if (predecessor == null || Utils.in_range(node.identifier, predecessor.identifier, info.identifier, false))
            predecessor(node);
    }

    private void fixFinger(Integer i) {

        // Update finger table entry
        finger_table[i] = findSuccessor(Utils.start(info.identifier, i + 1));

        Chord.executor.schedule(
                () -> fixFinger((i + 1) % Chord.m),
                500,
                TimeUnit.MILLISECONDS
        );
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
                    .append(i)
                    .append(": ")
                    .append(finger_table[i])
                    .append('\n');
        }

        return sb.toString();
    }

    public static void main(String[] args) throws Exception {

        if (args[0].equals("SUPER")) {
            ChordNode.instance().initSuperNode();
        } else if (args[0].equals("DEBUG")) {
            new ChordConnection(new InetSocketAddress(args[1], Integer.parseInt(args[2]))).debug();
        } else {
            ChordNode.instance().initNode(new InetSocketAddress(args[0], Integer.parseInt(args[1])));
        }
    }
}