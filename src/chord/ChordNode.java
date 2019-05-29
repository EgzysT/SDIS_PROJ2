package chord;

import common.chord.ChordConnection;
import common.chord.ChordDispatcher;
import common.protocol.ProtocolConnection;
import common.protocol.ProtocolDispatcher;
import common.protocol.ProtocolMessage;
import protocol.Protocol;
import store.ChunkInfo;
import store.Store;
import utils.Logger;
import utils.Utils;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static common.protocol.ProtocolMessage.Type.ACK;

// TODO if super node is dead, replace
// TODO check if other replicas are still alive
// TODO send replica list to reduce number of messages

/**
 * ChordHandler's node
 */
public class ChordNode implements ChordService {

    /** Singleton instance */
    private static ChordNode instance;

    /** Finger table */
    private Map<Integer, ChordInfo> finger_table;

    /** Successors */
    private Map<Integer, ChordInfo> successors;

    /** Predecessor */
    private ChordInfo predecessor;

    /** Node's info */
    public ChordInfo info;

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
     * @param addr Node's address
     */
    private void init(InetSocketAddress addr) {

        info = new ChordInfo(
                ChordHandler.hashToKey(Utils.generateChordID(addr.toString()), 0),
                addr,
                new InetSocketAddress(addr.getHostName(), 0)
        );

        finger_table = new ConcurrentHashMap<>();
        successors = new ConcurrentHashMap<>();
        predecessor = null;
    }

    /**
     * Initializes node's threads
     */
    private void initThreads() {

        try {
            // Chord's Dispatcher
            new Thread(
                    new ChordDispatcher(info.chordAddress.getPort())
            ).start();

            // Protocol's dispatcher
            new Thread(
                    new ProtocolDispatcher(0)
            ).start();

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        // Stabilize
        ChordHandler.executor.schedule(
                this::stabilize,
                50,
                TimeUnit.MILLISECONDS
        );

        // Fix finger
        ChordHandler.executor.schedule(
                () -> fixFinger(1),
                100,
                TimeUnit.MILLISECONDS
        );

        // Check predecessor
        ChordHandler.executor.schedule(
                this::checkPredecessor,
                150, TimeUnit.MILLISECONDS
        );

        // Transfer keys
        ChordHandler.executor.schedule(
                this::transferKeys,
                200,
                TimeUnit.MILLISECONDS
        );
    }

    /**
     * Get node first successor that is alive
     * @return Node's successor
     */
    public ChordInfo successor() {
        for (ChordInfo successor : successors.values()) {
            if (!successor.equals(info) && !new ChordConnection(successor.chordAddress).alive()) {
                clearNode(successor);
            } else {
                return successor;
            }
        }

        return info;
    }

    /**
     * Get node's successor list
     * @return Node's successor list
     */
    public List<ChordInfo> successors() {
        return new ArrayList<>(successors.values());
    }

    /**
     * Set's node's successor, both in finger table and successor list
     * @param node Node's successor
     */
    private void successor(ChordInfo node) {
        finger_table.put(1, node);
        successors.put(1, node);
    }

    /**
     * Get node's predecessor
     * @return Node's predecessor
     */
    public ChordInfo predecessor() {
        return predecessor;
    }

    /**
     * Finds successor of given key
     * @param key Identifier
     * @return Successor of given key
     */
    public ChordInfo findSuccessor(BigInteger key) {

        ChordInfo node = info, successor = successor();

        while (!Utils.inRange(key, node.identifier, successor.identifier, true)) {

            ChordInfo nextNode, nextSuccessor;

            if (node.equals(info))
                nextNode = closestPrecedingNode(key);
            else
                nextNode = new ChordConnection(node.chordAddress).findClosest(key);

            // If there was an error, ignore and try later
            if (nextNode == null)
                continue;

            if (nextNode.equals(info))
                nextSuccessor = successor();
            else
                nextSuccessor = new ChordConnection(nextNode.chordAddress).getSuccessor();

            // If there was an error, ignore and try later
            if (nextSuccessor == null)
                continue;

            node = nextNode;
            successor = nextSuccessor;
        }

        return successor;
    }

    /**
     * Finds closest preceding node of given key in the finger table
     * @param key Identifier
     * @return Closest preceding node of given key
     */
    public ChordInfo closestPrecedingNode(BigInteger key) {

        // Search in finger table
        for (int i = ChordHandler.m; i > 0; i--) {

            if (finger_table.get(i) == null)
                continue;

            if (Utils.inRange(finger_table.get(i).identifier, info.identifier, key, false)) {

                if (!finger_table.get(i).equals(info) && !new ChordConnection(finger_table.get(i).chordAddress).alive())
                    clearNode(finger_table.get(i));
                else
                    return finger_table.get(i);
            }
        }

        // Search in successor list
        for (int i = ChordHandler.r; i > 0; i--) {

            if (successors.get(i) == null)
                continue;

            if (Utils.inRange(successors.get(i).identifier, info.identifier, key, false)) {

                if (!successors.get(i).equals(info) && !new ChordConnection(successors.get(i).chordAddress).alive())
                    clearNode(successors.get(i));
                else
                    return successors.get(i);
            }
        }

        return info;
    }

    /**
     * Checks if given node is predecessor
     * @param node Possible predecessor node
     */
    public void notify(ChordInfo node) {

        if (predecessor == null || Utils.inRange(node.identifier, predecessor.identifier, info.identifier, false)) {
            predecessor = node;
            Logger.fine("Chord", "updated predecessor");
        }
    }

    /**
     * Checks if predecessor is alive
     */
    private void checkPredecessor() {

        if (predecessor != null && !predecessor.equals(info) && !new ChordConnection(predecessor.chordAddress).alive()) {
            clearNode(predecessor());

            if (ChordHandler.supernode.equals(info.chordAddress))
                predecessor = info;
            else
                predecessor = null;

            Logger.fine("Chord", "predecessor not alive, updating");
        }

        ChordHandler.executor.schedule(
                this::checkPredecessor,
                1000,
                TimeUnit.MILLISECONDS
        );
    }

    /**
     * Stabilizes node and notifies successor of node's existence
     */
    private void stabilize() {

        ChordInfo pred;

        if (successor().equals(info))
            pred = predecessor();
        else
            pred = new ChordConnection(successor().chordAddress).getPredecessor();

        // If connection failed, ask super node
        if (pred == null) {

            if (ChordHandler.supernode.equals(info.chordAddress))
                successor(findSuccessor(info.identifier));
            else
                successor(new ChordConnection(ChordHandler.supernode).findSuccessor(info.identifier));

        } else if (Utils.inRange(pred.identifier, info.identifier, successor().identifier, false)) {
            successor(pred);
        }

        if (successor().equals(info))
            notify(info);
        else
            new ChordConnection(successor().chordAddress).notify(info);

        List<ChordInfo> succ;

        if (successor().equals(info))
            succ = new ArrayList<>(successors.values());
        else
            succ = new ChordConnection(successor().chordAddress).getSuccessors();

        if (succ != null) {
            for (int i = 0; i < succ.size(); i++) {
                successors.put(i + 2, succ.get(i));
            }
        }

//        System.out.println(ChordNode.instance());

//        Logger.fine("Chord", "stabilized node");

        ChordHandler.executor.schedule(
                this::stabilize,
                1000,
                TimeUnit.MILLISECONDS
        );
    }

    /**
     * Fixes finger i of node's finger table
     * @param i Finger's number
     */
    private void fixFinger(Integer i) {

        BigInteger start = Utils.start(info.identifier, i);

        if (i.equals(1))
            successor(findSuccessor(start));
        else
            finger_table.put(i, findSuccessor(start));

//        Logger.fine("Chord", "updated finger " + i);

        ChordHandler.executor.schedule(
                () -> fixFinger(i % ChordHandler.m + 1),
                1000,
                TimeUnit.MILLISECONDS
        );
    }

    /**
     * Clears dead node from both the finger table and successor list
     * @param node Dead node
     */
    private void clearNode(ChordInfo node) {

        // Clear from finger table
        for (Map.Entry<Integer, ChordInfo> finger : finger_table.entrySet()) {
            if (finger.getValue().equals(node))
                finger_table.remove(finger.getKey());
        }

        // Clear from successor list
        for (Map.Entry<Integer, ChordInfo> successor : successors.entrySet()) {
            if (successor.getValue().equals(node))
                successors.remove(successor.getKey());
        }
    }

    private void transferKeys() {

        System.out.println("Checking keys");

        for (String fileID : Store.chunks.keySet()) {
            for (Map.Entry<Integer, ChunkInfo> chunk : Store.chunks.get(fileID).entrySet()) {

                for (int i = 0; i < ChordHandler.repDeg; i++) {

                    if (chunk != null && chunk.getValue().replicas.contains(i)) {

                        ChordInfo n = findSuccessor(ChordHandler.hashToKey(fileID + chunk.getKey(), i));

                        if (!n.equals(info)) {
                            System.out.println("Transferring #" + chunk.getKey() + "[" + i + "]" + " to " + n.identifier);
                            Protocol.transferChunk(n.protocolAddress, fileID, chunk.getKey(), i);
                        }
                    }
                }
            }
        }

        ChordHandler.executor.schedule(
                this::transferKeys,
                2000,
                TimeUnit.MILLISECONDS
        );
    }

    @Override
    public void join(InetSocketAddress nodeAddr, InetSocketAddress superAddr) {

        init(nodeAddr);

        if (superAddr != null) {

            ChordHandler.supernode = superAddr;

            if (!new ChordConnection(ChordHandler.supernode).alive()) {
                Logger.info("Chord", "super node is not alive");
                return;
            }

            successor(new ChordConnection(ChordHandler.supernode).findSuccessor(info.identifier));

            Logger.info("Chord", "starting node " + info.identifier + " at " + info.chordAddress);

        } else {

            ChordHandler.supernode = nodeAddr;

            successor(info);

            Logger.info("Chord", "starting super node " + info.identifier + " at " + info.chordAddress);
        }

        initThreads();
    }

    @Override
    public BigInteger id() {
        return info.identifier;
    }

    @Override
    public void put(String fileID, Integer chunkNo, byte[] chunk) {

        for (int i = 0; i < ChordHandler.repDeg; i++) {

            ChordInfo n = findSuccessor(ChordHandler.hashToKey(fileID + chunkNo, i));

            ProtocolMessage reply = new ProtocolConnection(n.protocolAddress).backupChunk(fileID, chunkNo, chunk, i);

            // Repeat if there was an error in connection, ignore if received NACK
            if (reply == null) {
                i--;
            } else if (reply.type == ACK) {
                Logger.fine("Chord", "node " + n.identifier + " stored chunk #" + chunkNo + " from file " + fileID);
            }
        }
    }

    @Override
    public byte[] get(String fileID, Integer chunkNo) {

        for (int i = 0; i < ChordHandler.repDeg; i++) {

            ChordInfo n = findSuccessor(ChordHandler.hashToKey(fileID + chunkNo, i));

            ProtocolMessage reply = new ProtocolConnection(n.protocolAddress).restoreChunk(fileID, chunkNo);

            // Repeat if there was an error in connection, ignore if received NACK
            if (reply == null) {
                i--;
            } else if (reply.type == ACK) {
                Logger.fine("Chord", "node " + n.identifier + " restored chunk #" + chunkNo + " from file " + fileID);
                return reply.chunk;
            }
        }

        return null;
    }

    @Override
    public void remove(String fileID, Integer chunkNo) {

        for (int i = 0; i < ChordHandler.repDeg; i++) {

            ChordInfo n = findSuccessor(ChordHandler.hashToKey(fileID + chunkNo, i));

            ProtocolMessage reply = new ProtocolConnection(n.protocolAddress).deleteChunk(fileID, chunkNo);

            // Repeat if there was an error in connection, ignore if received NACK
            if (reply == null) {
                i--;
            } else if (reply.type == ACK) {
                Logger.fine("Chord", "node " + n.identifier + " deleted chunk #" + chunkNo + " of file " + fileID);
            }
        }
    }

    @Override
    public String toString() {

//        Utils.clearScreen();

        StringBuilder sb = new StringBuilder();

        sb.append("Node ")
                .append(info.identifier)
                .append(" at ")
                .append(info.chordAddress)
                .append("\nPredecessor: ")
                .append(predecessor())
                .append('\n');

        for (int i = 1; i <= ChordHandler.m; i++) {
            sb.append("Finger #")
                    .append(i)
                    .append(": ")
                    .append(finger_table.get(i))
                    .append('\n');
        }

        for (int i = 1; i <= ChordHandler.r; i++) {
            sb.append("Successor #")
                    .append(i)
                    .append(": ")
                    .append(successors.get(i))
                    .append('\n');
        }

        return sb.toString();
    }
}