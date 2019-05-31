package common.chord;

import chord.ChordInfo;
import common.Message;

import java.math.BigInteger;
import java.util.List;

/**
 * Chord message
 */
class ChordMessage extends Message {

    /**
     * Message types
     */
    enum Type {
        CLOSEST_PRECEDING_NODE,
        FIND_SUCCESSOR, NOTIFY,
        GET_SUCCESSOR, GET_SUCCESSORS, GET_PREDECESSOR,
        NODE, ALIVE
    }

    /** Message type */
    Type type;

    /** Key */
    BigInteger key;

    /** Node */
    ChordInfo node;

    /** Nodes */
    List<ChordInfo> nodes;

    /**
     * Creates a new message
     * @param type Message type
     */
    ChordMessage(Type type) {
        this.type = type;
    }

    /**
     * Creates a new message
     * @param type Message type
     * @param key Key
     */
    ChordMessage(Type type, BigInteger key) {
        this.type = type;
        this.key = key;
    }

    /**
     * Creates a new message
     * @param type Message type
     * @param node Node
     */
    ChordMessage(Type type, ChordInfo node) {
        this.type = type;
        this.node = node;
    }

    /**
     * Creates a new message
     * @param type Message type
     * @param nodes Nodes
     */
    ChordMessage(Type type, List<ChordInfo> nodes) {
        this.type = type;
        this.nodes = nodes;
    }
}