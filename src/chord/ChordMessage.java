package chord;

import java.io.Serializable;

public class ChordMessage implements Serializable {

    enum MessageType {
        CLOSEST_PRECEDING_NODE,
        FIND_SUCCESSOR, NOTIFY,
        GET_SUCCESSOR, GET_PREDECESSOR,
        NODE, DEBUG, END
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

    public String toString() {
        StringBuilder sb = new StringBuilder()
                .append(type.toString())
                .append(" ");

        if (key != null)
            sb.append(key);
        else
            sb.append(node);

        return sb.toString();
    }
}