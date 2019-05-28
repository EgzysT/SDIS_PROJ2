package common.protocol;

import common.Message;

class ProtocolMessage extends Message {

    enum Type {
        BACKUP,
        RESTORE,
        DELETE,
        ACK, NACK
    }

    Type type;
    String fileID;
    Integer chunkNo, i;
    byte[] chunk;

    ProtocolMessage(Type type) {
        this.type = type;
    }

    ProtocolMessage(Type type, String fileID, Integer chunkNo, byte[] chunk, Integer i) {
        this.type = type;
        this.fileID = fileID;
        this.chunkNo = chunkNo;
        this.chunk = chunk;
        this.i = i;
    }

    ProtocolMessage(Type type, String fileID, Integer chunkNo) {
        this.type = type;
        this.fileID = fileID;
        this.chunkNo = chunkNo;
    }

    ProtocolMessage(Type type, byte[] chunk) {
        this.type = type;
        this.chunk = chunk;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder()
                .append(type.toString())
                .append(" ");

        if (fileID != null)
            sb.append(fileID);
        if (chunkNo != null)
            sb.append(" ").append(chunkNo);

        return sb.toString();
    }

}
