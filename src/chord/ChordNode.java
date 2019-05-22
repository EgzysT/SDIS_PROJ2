package chord;

import common.chord.ChordConnection;
import common.chord.ChordDispatcher;
import common.protocol.ProtocolConnection;
import common.protocol.ProtocolDispatcher;
import store.Store;
import utils.Logger;
import utils.Utils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Chord's node
 */
public final class ChordNode implements ChordService {

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
     * @param addr Address
     */
    private void init(InetSocketAddress addr) {

        info = new ChordInfo(
                Chord.hashToKey(Utils.generateChordID(addr.toString())),
                addr,
                new InetSocketAddress("localhost", 0)
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
                    new ProtocolDispatcher(info.protocolAddress.getPort())
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
     * Get node's successor
     * @return Node's successor
     */
    public ChordInfo successor() {
        for (ChordInfo successor : successors.values()) {
            if (!successor.equals(info) && !new ChordConnection(successor.chordAddress).alive())
                clearNode(successor);
            else
                return successor;
        }

        return info;
    }

    public List<ChordInfo> successors() {
        return new ArrayList<>(successors.values());
    }

    /**
     * Get node's predecessor
     * @return Node's predecessor
     */
    public ChordInfo predecessor() {
        return predecessor;
    }

    void successor(ChordInfo node) {
        finger_table.put(1, node);
        successors.put(1, node);
    }

    /**
     * Finds successor of given key
     * @param key Identifier
     * @return Successor of given key
     */
    public ChordInfo findSuccessor(Integer key) {

        ChordInfo node = info;
        ChordInfo successor = successor();

        while (!Utils.in_range(key, node.identifier, successor.identifier, true)) {

            if (node.equals(info))
                node = closestPrecedingNode(key);
            else
                node = new ChordConnection(node.chordAddress).findClosest(key);

            if (node.equals(info))
                successor = successor();
            else
                successor = new ChordConnection(node.chordAddress).getSuccessor();
        }

        return successor;
    }

    /**
     * Finds closest preceding node of given key in the finger table
     * @param key Identifier
     * @return Closest preceding node of given key
     */
    public ChordInfo closestPrecedingNode(Integer key) {

        for (int i = Chord.m; i > 0; i--) {

            if (finger_table.get(i) == null)
                continue;

            if (Utils.in_range(finger_table.get(i).identifier, info.identifier, key, false)) {

                if (!finger_table.get(i).equals(info) && !new ChordConnection(finger_table.get(i).chordAddress).alive())
                    clearNode(finger_table.get(i));
                else
                    return finger_table.get(i);
            }
        }

        for (int i = Chord.r; i > 0; i--) {

            if (successors.get(i) == null)
                continue;

            if (Utils.in_range(successors.get(i).identifier, info.identifier, key, false)) {

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

        if (predecessor == null || Utils.in_range(node.identifier, predecessor.identifier, info.identifier, false)) {
            predecessor = node;
            Logger.fine("Chord", "updated predecessor");
        }
    }

    /**
     * Checks if predecessor is alive
     */
    private void checkPredecessor() {

        if (predecessor != null && !predecessor.equals(info) && !new ChordConnection(predecessor.chordAddress).alive()) {
            clearNode(predecessor);

            if (Chord.supernode.equals(info.chordAddress))
                predecessor = info;
            else
                predecessor = null;

            Logger.fine("Chord", "predecessor not alive, updating");
        }

        Chord.executor.schedule(
            this::checkPredecessor,
            200,
            TimeUnit.MILLISECONDS
        );
    }

    /**
     * Stabilizes node and notifies successor of node's existence
     */
    private void stabilize() {

        ChordInfo pred;

        if (successor().equals(info))
            pred = predecessor;
        else
            pred = new ChordConnection(successor().chordAddress).getPredecessor();

        if (pred == null) {
            if (Chord.supernode.equals(info.chordAddress))
                successor(findSuccessor(info.identifier));
            else
                successor(new ChordConnection(Chord.supernode).findSuccessor(info.identifier));

            if (finger_table.get(1) == null)
                successor(info);

        } else if (Utils.in_range(pred.identifier, info.identifier, successor().identifier, false)) {
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

        for (int i = 2; i < succ.size() + 2; i++) {
            successors.put(i, succ.get(i - 2));
        }

        Logger.fine("Chord", "stabilized node");

        Chord.executor.schedule(
                this::stabilize,
                200,
                TimeUnit.MILLISECONDS
        );
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
                200,
                TimeUnit.MILLISECONDS
        );
    }

    private void clearNode(ChordInfo node) {

        for (Map.Entry<Integer, ChordInfo> finger : finger_table.entrySet()) {
            if (finger.getValue().equals(node))
                finger_table.remove(finger.getKey());
        }

        for (Map.Entry<Integer, ChordInfo> successor : successors.entrySet()) {
            if (successor.getValue().equals(node))
                successors.remove(successor.getKey());
        }
    }

    /**
     * Initializes super node
     */
    @Override
    public void createSuperNode() {

        init(Chord.supernode);

        successor(info);

        initThreads();

        Logger.info("Chord", "starting super node " + info.identifier + " at " + info.chordAddress);
    }

    /**
     * Initializes node
     * @param addr Address
     */
    @Override
    public void createNode(InetSocketAddress addr) {

        init(addr);

        if (!new ChordConnection(Chord.supernode).alive()) {
            Logger.info("Chord", "super node is not alive");
            return;
        }

        successor(new ChordConnection(Chord.supernode).findSuccessor(info.identifier));

        initThreads();

        Logger.info("Chord", "starting node " + info.identifier + " at " + info.chordAddress);
    }

    @Override
    public Integer id() {
        return info.identifier;
    }

    @Override
    public void put(String fileID, Integer chunkNo, byte[] chunk)  {
        ChordInfo responsibleNode = findSuccessor(Chord.hashToKey(fileID + chunkNo));

        Boolean status = new ProtocolConnection(responsibleNode.protocolAddress).backupChunk(fileID, chunkNo, chunk);

        // TODO Error
        if (status == null) {
            System.out.println("Error in connection");
        } else if (status) {
            Store.registerChunk(fileID, chunkNo, responsibleNode.identifier);
            Logger.fine("Chord", "node " + responsibleNode.identifier + " confirmed store " +
                    "for chunk #" + chunkNo + " from file " + fileID);
        } else {
            System.out.println("ERROR");
        }
    }

    @Override
    public byte[] get(String fileID, Integer chunkNo) {
        ChordInfo responsibleNode = findSuccessor(Chord.hashToKey(fileID + chunkNo));

        byte[] chunk = new ProtocolConnection(responsibleNode.protocolAddress).restoreChunk(fileID, chunkNo);

        if (chunk == null) {
            System.out.println("Error in connection");
        } else {
            Logger.fine("Chord", "recovered chunk #" + chunkNo + " from file " + fileID +
                    " from node " + responsibleNode.identifier);
        }

        return chunk;
    }

    @Override
    public String toString() {

        Utils.clearScreen();

        StringBuilder sb = new StringBuilder();

        sb.append("Node ")
                .append(info.identifier)
                .append(" at ")
                .append(info.chordAddress)
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
}