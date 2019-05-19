package protocol;

import core.Message;

public class ProtocolMessage extends Message {

    enum MessageType {
        BACKUP,
        RESTORE,
        DELETE,
        OK, FAIL
    }

    ProtocolMessage() {


    }




}
