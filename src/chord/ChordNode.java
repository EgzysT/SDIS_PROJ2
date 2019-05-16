package chord;

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

    void initSuperNode() {

        init(Chord.supernode);

        predecessor(info);
        successor(info);

        initThreads();

        Logger.info("Chord", "starting super node " + info.identifier + " at " + info.address);
    }

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

    NodeInfo findSuccessor(Integer key) {

        NodeInfo node = info;
        NodeInfo successor = successor();

        while (!Utils.in_range(key, node.identifier, successor.identifier, true)) {

            if (node.equals(info))
                node = closestPrecedingNode(key);
            else
                node = new ChordConnection(node.address).findClosest(key);

            // TODO
            if (node == null) {
                node = info;
                System.out.println("Shouldn't happen1");
            }

            if (node.equals(info))
                successor = successor();
            else
                successor = new ChordConnection(node.address).getSuccessor();

            // TODO
            if (successor == null) {
                successor = info;
                System.out.println("Shouldn't happen2");
            }
        }

        return successor;
    }

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

    private void stabilize() {

        NodeInfo x;

        if (successor().equals(info))
            x = predecessor();
        else
            x = new ChordConnection(successor().address).getPredecessor();

//        System.out.println("asked node " + successor().identifier + " for predecessor");

        if (x == null) {
            if (Chord.supernode.equals(info.address))
                successor(findSuccessor(info.identifier));
            else
                successor(new ChordConnection(Chord.supernode).findSuccessor(info.identifier));

            // TODO
            if (successor() == null) {
                successor(info);
                System.out.println("Shouldn't happen3");
            }

            Logger.fine("Chord", "successor not alive, updating");
        } else {
            if (Utils.in_range(x.identifier, info.identifier, successor().identifier, false)) {
                successor(x);
                Logger.fine("Chord", "updated successor");
            }

            new ChordConnection(successor().address).notify(info);
        }

        Chord.executor.schedule(
                this::stabilize,
                2,
                TimeUnit.SECONDS
        );
    }

    void notify(NodeInfo node) {

        if (predecessor == null || Utils.in_range(node.identifier, predecessor.identifier, info.identifier, false)) {
            predecessor(node);
            Logger.fine("Chord", "updated predecessor");
        }
    }

    private void fixFinger(Integer i) {

        finger_table[i] = findSuccessor(Utils.start(info.identifier, i + 1));

        // TODO
        if (finger_table[i] == null) {
            finger_table[i] = info;
            System.out.println("Shouldn't happen4");
        }

        Logger.fine("Chord", "updated finger " + (i + 1));

        Chord.executor.schedule(
                () -> fixFinger((i + 1) % Chord.m),
                2,
                TimeUnit.SECONDS
        );
    }

    NodeInfo successor() {
        return finger_table[0];
    }

    NodeInfo predecessor() {
        return predecessor;
    }

    private void successor(NodeInfo node) {
        finger_table[0] = node;
    }

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

        if (args[0].equals("SUPER")) {
            ChordNode.instance().initSuperNode();
        } else if (args[0].equals("DEBUG")) {
            new ChordConnection(new InetSocketAddress(args[1], Integer.parseInt(args[2]))).debug();
        } else {
            ChordNode.instance().initNode(new InetSocketAddress(args[0], Integer.parseInt(args[1])));
        }
    }
}