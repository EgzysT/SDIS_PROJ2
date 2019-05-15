package chord;

import utils.Logger;
import utils.Utils;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.InetSocketAddress;

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

    private static ChordNode instance;
    private static Integer identifier;
    private static InetSocketAddress address;
    private static NodeInfo[] finger_table;
    private static NodeInfo predecessor;

    private ChordNode() {}

    static ChordNode instance() {
        if (instance == null)
            instance = new ChordNode();
        return instance;
    }

    void initSuperNode() {
        identifier = Utils.hash(Chord.supernode.toString()).mod(new BigInteger("2").pow(Chord.m)).intValue();
        address = Chord.supernode;
        finger_table = new NodeInfo[Chord.m];

        NodeInfo info = new NodeInfo(identifier, address);

        predecessor = info;

        for (int i = 0; i < Chord.m; i++) {
            finger_table[i] = info;
        }

        try {
            new Thread(new ChordDispatcher(address.getPort())).start();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        Logger.info("Chord", "starting super node " + identifier + " at " + address);
    }

    void initNode(InetSocketAddress addr) {
        identifier = Utils.hash(addr.toString()).mod(new BigInteger("2").pow(Chord.m)).intValue();
        address = addr;
        finger_table = new NodeInfo[Chord.m];

        try {
            // Open connection with supernode
            ChordConnection connectionSN = new ChordConnection(Chord.supernode);

            // Ask supernode for successor
            // Start: n + 2^(1 - 1) <=> n + 2^0 <=> n + 1
            connectionSN.send(new ChordMessageKey(identifier + 1).type(ChordMessage.MessageType.FIND_SUCCESSOR));
            finger_table[0] = ((ChordMessageNode) connectionSN.receive()).info;

            // Open connection with successor
            ChordConnection connectionS = new ChordConnection(successor().address);

            // Ask successor for predecessor
            connectionS.send(new ChordMessage().type(ChordMessage.MessageType.GET_PREDECESSOR));
            predecessor = ((ChordMessageNode) connectionS.receive()).info;

            // Update successor's predecessor
            connectionS.send(new ChordMessageNode(info()).type(ChordMessage.MessageType.SET_PREDECESSOR));
            connectionS.receive();

            // Close connection with successor
            connectionS.send(new ChordMessage().type(ChordMessage.MessageType.END));
            connectionS.close();

            for (int i = 0; i < Chord.m - 1; i++) {
                // Finger: n + 2^(i + 1 - 1) <=> n + 2^(i)
                int start = identifier + (int) Math.pow(2, i);

                if (start >= identifier && start < finger_table[i].identifier) {
                    finger_table[i + 1] = finger_table[i];
                } else {
                    // Ask supernode for successor
                    connectionSN.send(new ChordMessageKey(start).type(ChordMessage.MessageType.FIND_SUCCESSOR));
                    finger_table[i + 1] = ((ChordMessageNode) connectionSN.receive()).info;
                }
            }

            System.out.println("\n--- DEBUG ---");
            System.out.println(this);
            System.out.println("-------------\n");

            // Debug
            connectionSN.send(new ChordMessage().type(ChordMessage.MessageType.DEBUG));

            // Close connection with supernode
            connectionSN.send(new ChordMessage().type(ChordMessage.MessageType.END));
            connectionSN.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        Logger.info("Chord", "starting node " + identifier + " at " + address);
    }

    NodeInfo successor() {
        return finger_table[0];
    }

    NodeInfo predecessor() {
        return predecessor;
    }

    void predecessor(NodeInfo info) {
        predecessor = info;
    }

    NodeInfo info() {
        return new NodeInfo(identifier, address);
    }

    NodeInfo findSuccessor(Integer key) {

        NodeInfo node = findPredecessor(key);
        NodeInfo successor = null;

        try {
            // Open connection
            ChordConnection connection = new ChordConnection(node.address);

            // Ask node's successor
            connection.send(new ChordMessage().type(ChordMessage.MessageType.GET_SUCCESSOR));
            successor = ((ChordMessageNode) connection.receive()).info;

            // Close connection
            connection.send(new ChordMessage().type(ChordMessage.MessageType.END));
            connection.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        return successor;
    }

    NodeInfo findPredecessor(Integer key) {

        NodeInfo node = new NodeInfo(identifier, address);
        NodeInfo successor = successor();

        while (!(key > node.identifier && key <= successor.identifier)) {

//            try {
//                // Open connection
//                ChordConnection connection = new ChordConnection(node.address);
//
//                // Ask closest preceding node and update
//                connection.send(new ChordMessageKey(key).type(ChordMessage.MessageType.CLOSEST_PRECEDING_FINGER));
//                node = ((ChordMessageNode) connection.receive()).info;
//
//                // Ask closest preceding node's successor
//                connection.send(new ChordMessage().type(ChordMessage.MessageType.GET_SUCCESSOR));
//                successor = ((ChordMessageNode) connection.receive()).info;
//
//                // Close connection
//                connection.send(new ChordMessage().type(ChordMessage.MessageType.END));
//                connection.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//                System.exit(-1);
//            }

            break;
        }

        return node;
    }

    NodeInfo closestPrecedingFinger(Integer key) {

        for (int i = Chord.m - 1; i >= 0; i--) {
            NodeInfo finger = finger_table[i];
            if (finger.identifier > identifier && finger.identifier < key)
                return finger;
        }

        return new NodeInfo(identifier, address);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Node ")
                .append(identifier)
                .append(" at ")
                .append(address)
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

    public static void main(String[] args) {

        if (args[0].equals("SUPER"))
            ChordNode.instance().initSuperNode();
        else
            ChordNode.instance().initNode(new InetSocketAddress(args[0], Integer.parseInt(args[1])));

    }
}



















//import java.net.InetSocketAddress;

//class Finger {
//    Integer start;
//    InetSocketAddress node;
//
//    Finger(Integer start, InetSocketAddress node) {
//        this.start = start;
//        this.node = node;
//    }
//}


//public final class Node {
//    public Integer identifier;
//    private InetSocketAddress address;
//    private Finger[] finger_table;
//
//    private InetSocketAddress predecessor;
//
//    /*
//        entry i contains
//        s = successor(n + 2^(i - 1))
//
//        first finger is called successor
//
//     */
//
//    public Node() {
//
//        // add idk
//
//        identifier = 0;
//        address = new InetSocketAddress("127.0.0.1", 8080);
//        finger_table = new Finger[Chord.m];
//        predecessor = address;
//
//        for (int i = 0; i < Chord.m; i++) {
////            finger_table[i] = new Finger( identifier + (int) Math.pow(2, i - 1) , address);
//            finger_table[i] = new Finger(identifier, address);
//        }
//    }
//
//    /*
//    The data corresponding to the key k is being held in the node with ID successor(k).
//    In order to find it, we find the immediate predecessor node of the desired k; the successor
//    of that node must be the successor(k).
//     */
//
//    Finger successor() {
//        return finger_table[0];
//    }
//
//    InetSocketAddress successor(Integer id) {
//
//
//
//        // For m down to 1
//        for (Integer i = Chord.m; i > 0; i--) {
//
//            Integer start = finger_table[i - 1].start;
//
//            if (start )
//
//
//
//
//
//
//        }
//
//
//
//
//
//        InetSocketAddress node = find_predecessor(id);
//        ret
//    }
//
//    InetSocketAddress find_predecessor(Integer key) {
//
//        InetSocketAddress n = address;
//        Integer id = identifier;
//        Integer succ = successor().start;
//
//        while (key > id && key <= succ) {
//            n = closest_preceding_finger(key);
//
//        }
//
//        return n;
//    }
//
//    InetSocketAddress closest_preceding_finger(Integer key) {
//
//        for (Integer i = Chord.m; i > 0; i--) {
//            Finger finger = finger_table[i];
//            if (finger.start > identifier && finger.start < key)
//                return finger.node;
//        }
//
//        return address;
//    }
//
//
//
//}
