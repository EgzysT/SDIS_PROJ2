package common.chord;

import chord.ChordInfo;
import common.Message;

import java.util.List;

class ChordMessage extends Message {

    enum Type {
        CLOSEST_PRECEDING_NODE,
        FIND_SUCCESSOR, NOTIFY,
        GET_SUCCESSOR, GET_SUCCESSORS, GET_PREDECESSOR,
        NODE, ALIVE
    }

    Type type;
    Integer key;
    ChordInfo node;
    List<ChordInfo> nodes;

    ChordMessage(Type type) {
        this.type = type;
    }

    ChordMessage(Type type, Integer key) {
        this.type = type;
        this.key = key;
    }

    ChordMessage(Type type, ChordInfo node) {
        this.type = type;
        this.node = node;
    }

    ChordMessage(Type type, List<ChordInfo> nodes) {
        this.type = type;
        this.nodes = nodes;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder()
                .append(type.toString())
                .append(" ");

        if (key != null)
            sb.append(key);
        if (node != null)
            sb.append(node.chordAddress);

        return sb.toString();
    }
}