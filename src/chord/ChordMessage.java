package chord;

import java.io.Serializable;

public class ChordMessage implements Serializable {

    enum MessageType {
        CLOSEST_PRECEDING_FINGER,
        FIND_SUCCESSOR, FIND_PREDECESSOR,
        GET_SUCCESSOR, GET_PREDECESSOR,
        SET_PREDECESSOR,
        NODE, OK, DEBUG, END
    }

    MessageType type;

    ChordMessage type(MessageType type) {
        this.type = type;
        return this;
    }

    public String toString() {
        return type.toString();
    }
}

class ChordMessageKey extends ChordMessage {

    Integer key;

    ChordMessageKey(Integer key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return super.toString() + " " + key;
    }
}

class ChordMessageNode extends ChordMessage {

    NodeInfo info;

    ChordMessageNode(NodeInfo info) {
        this.info = info;
    }

    @Override
    public String toString() {
        return super.toString() + " " + info;
    }
}