package chord;

import core.Message;

public class ChordMessage extends Message {

    enum MessageType {
        CLOSEST_PRECEDING_NODE,
        FIND_SUCCESSOR, NOTIFY,
        GET_SUCCESSOR, GET_PREDECESSOR,
        ALIVE, NODE, DEBUG
    }

    MessageType type;
    Integer key;
    NodeInfo node;

    ChordMessage() {}

    ChordMessage(Integer key) {
        this.key = key;
    }

    ChordMessage(NodeInfo node) {
        this.node = node;
    }

    ChordMessage type(MessageType type) {
        this.type = type;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder()
                .append(type.toString())
                .append(" ");

        if (key != null)
            sb.append(key);
        if (node != null)
            sb.append(node.address);

        return sb.toString();
    }
}