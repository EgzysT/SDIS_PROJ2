package chord;

import common.chord.ChordConnection;
import common.chord.ChordDispatcher;
import common.protocol.ProtocolConnection;
import common.protocol.ProtocolDispatcher;
import store.Store;
import utils.Logger;
import utils.Utils;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

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
     * @param addr Address
     */
    private void init(InetSocketAddress addr) {

        info = new ChordInfo(
                ChordHandler.hashToKey(Utils.generateChordID(addr.toString())),
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
            // ChordHandler's Dispatcher
            new Thread(
                    new ChordDispatcher(info.chordAddress.getPort())
            ).start();

            // Protocol's dispatcher
            new Thread(
                    new ProtocolDispatcher(info.protocolAddress.getPort())
            ).start();

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
                    150,
                    TimeUnit.MILLISECONDS
            );

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
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
     * Get node's successors
     * @return Node's successors
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

        ChordInfo node = info;
        ChordInfo successor = successor();

        while (!Utils.inRange(key, node.identifier, successor.identifier, true)) {

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
    public ChordInfo closestPrecedingNode(BigInteger key) {

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
            Logger.fine("ChordHandler", "updated predecessor");
        }
    }

    /**
     * Checks if predecessor is alive
     */
    private void checkPredecessor() {

        if (predecessor != null && !predecessor.equals(info) && !new ChordConnection(predecessor.chordAddress).alive()) {
            clearNode(predecessor);

            if (ChordHandler.supernode.equals(info.chordAddress))
                predecessor = info;
            else
                predecessor = null;

            Logger.fine("ChordHandler", "predecessor not alive, updating");
        }

        ChordHandler.executor.schedule(
            this::checkPredecessor,
            500,
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
            if (ChordHandler.supernode.equals(info.chordAddress))
                successor(findSuccessor(info.identifier));
            else
                successor(new ChordConnection(ChordHandler.supernode).findSuccessor(info.identifier));

            if (finger_table.get(1) == null)
                successor(info);

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

        System.out.println(ChordNode.instance());

        Logger.fine("ChordHandler", "stabilized node");

        ChordHandler.executor.schedule(
                this::stabilize,
                500,
                TimeUnit.MILLISECONDS
        );
    }

    /**
     * Fixes finger i of node's finger table
     * @param i Finger's number
     */
    private void fixFinger(Integer i) {

//        BigInteger start = new BigInteger(info.identifier + (int) Math.pow(2, i - 1));

        BigInteger start = Utils.start(info.identifier, i);

        if (i.equals(1))
            successor(findSuccessor(start));
        else
            finger_table.put(i, findSuccessor(start));

        Logger.fine("ChordHandler", "updated finger " + i);

        ChordHandler.executor.schedule(
                () -> fixFinger(i % ChordHandler.m + 1),
                500,
                TimeUnit.MILLISECONDS
        );
    }

    /**
     * Clears dead node from both the finger table and successor list
     * @param node Dead node
     */
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

        init(ChordHandler.supernode);

        successor(info);

        initThreads();

        Logger.info("ChordHandler", "starting super node " + info.identifier + " at " + info.chordAddress);
    }

    /**
     * Initializes node
     * @param addr Address
     */
    @Override
    public void createNode(InetSocketAddress addr) {

        init(addr);

        if (!new ChordConnection(ChordHandler.supernode).alive()) {
            Logger.info("ChordHandler", "super node is not alive");
            return;
        }

        successor(new ChordConnection(ChordHandler.supernode).findSuccessor(info.identifier));

        initThreads();

        Logger.info("ChordHandler", "starting node " + info.identifier + " at " + info.chordAddress);
    }

    @Override
    public BigInteger id() {
        return info.identifier;
    }

    @Override
    public void put(String fileID, Integer chunkNo, byte[] chunk)  {
        ChordInfo responsibleNode = findSuccessor(ChordHandler.hashToKey(fileID + chunkNo));

        Boolean status = new ProtocolConnection(responsibleNode.protocolAddress).backupChunk(fileID, chunkNo, chunk);

        // TODO Error
        if (status == null) {
            System.out.println("Error in connection");
        } else if (status) {
            Store.registerChunk(fileID, chunkNo, responsibleNode.identifier);
            Logger.fine("ChordHandler", "node " + responsibleNode.identifier + " confirmed store " +
                    "for chunk #" + chunkNo + " from file " + fileID);
        } else {
            System.out.println("ERROR");
        }
    }

    @Override
    public byte[] get(String fileID, Integer chunkNo) {
        ChordInfo responsibleNode = findSuccessor(ChordHandler.hashToKey(fileID + chunkNo));

        byte[] chunk = new ProtocolConnection(responsibleNode.protocolAddress).restoreChunk(fileID, chunkNo);

        if (chunk == null) {
            System.out.println("Error in connection");
        } else {
            Logger.fine("ChordHandler", "recovered chunk #" + chunkNo + " from file " + fileID +
                    " from node " + responsibleNode.identifier);
        }

        return chunk;
    }

    @Override
    public void remove(String fileID, Integer chunkNo) {
        ChordInfo responsibleNode = findSuccessor(ChordHandler.hashToKey(fileID + chunkNo));

        Boolean status = new ProtocolConnection(responsibleNode.protocolAddress).deleteChunk(fileID, chunkNo);
        if (status == null) {    
            System.out.println("Error in connection");
        }
        else if (!status) {
            System.out.println("ERROR");
        }
        else {
            Store.unregisterChunk(fileID, chunkNo, responsibleNode.identifier);
            Logger.fine("Chord", "node " + responsibleNode.identifier + " confirmed delete " +
                    "for chunk #" + chunkNo + " from file " + fileID);
        }
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