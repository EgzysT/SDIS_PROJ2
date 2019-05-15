package chord;

/*
    QUERY_SUCCESSOR <identifier>
    SUCCESSOR <successor>

    QUERY_PREDECESSOR <identifier>
    PREDECESSOR <successor>

    UPDATE_PREDECESSOR <predecessor>
    UPDATED






    END

 */

import java.io.Serializable;

public class ChordMessage implements Serializable {

    enum MessageType {
        QUERY_SUCCESSOR, SUCESSOR,
        QUERY_PREDECESSOR, PREDECESSOR,
        END
    }

    MessageType type;;

    ChordMessage(MessageType type) {
        this.type = type;
    }

    public String toString() {
        return type.toString();
    }
}

class ChordRequestQUERY extends ChordMessage {

    Integer key;

    ChordRequestQUERY(MessageType type, Integer key) {
        super(type);
        this.key = key;
    }

    @Override
    public String toString() {
        return super.toString() + " " + key;
    }
}

class ChordReplyQUERY extends ChordMessage {

    NodeInfo info;

    ChordReplyQUERY(MessageType type, NodeInfo info) {
        super(type);
        this.info = info;
    }

    @Override
    public String toString() {
        return super.toString() + " " + info;
    }
}
