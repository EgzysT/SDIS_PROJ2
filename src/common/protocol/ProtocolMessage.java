package common.protocol;

import common.Message;

public class ProtocolMessage extends Message {

    public enum Type {
        BACKUP_CHUNK,
        RESTORE_CHUNK,
        DELETE_CHUNK,
        HAS_CHUNK,
        ACK, NACK
    }

    public Type type;
    public String fileID;
    public Integer chunkNo, replicaNo;
    public byte[] chunk;

    ProtocolMessage(Type type) {
        this.type = type;
    }

    ProtocolMessage(Type type, String fileID, Integer chunkNo, byte[] chunk, Integer replicaNo) {
        this.type = type;
        this.fileID = fileID;
        this.chunkNo = chunkNo;
        this.chunk = chunk;
        this.replicaNo = replicaNo;
    }

    ProtocolMessage(Type type, String fileID, Integer chunkNo) {
        this.type = type;
        this.fileID = fileID;
        this.chunkNo = chunkNo;
    }

    ProtocolMessage(Type type, String fileID, Integer chunkNo, Integer replicaNo) {
        this.type = type;
        this.fileID = fileID;
        this.chunkNo = chunkNo;
        this.replicaNo = replicaNo;
    }

    ProtocolMessage(Type type, byte[] chunk) {
        this.type = type;
        this.chunk = chunk;
    }
}
