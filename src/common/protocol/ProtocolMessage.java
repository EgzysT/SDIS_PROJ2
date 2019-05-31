package common.protocol;

import common.Message;

public class ProtocolMessage extends Message {

    /**
     * Message types
     */
    public enum Type {
        BACKUP_CHUNK,
        RESTORE_CHUNK,
        DELETE_CHUNK,
        HAS_CHUNK,
        ACK, NACK
    }

    /** Message type */
    public Type type;

    /** File identifier */
    public String fileID;

    /** Chunk number and replica number */
    public Integer chunkNo, replicaNo;

    /** Chunk */
    public byte[] chunk;

    /**
     * Creates a new message
     * @param type Message type
     */
    ProtocolMessage(Type type) {
        this.type = type;
    }

    /**
     * Creates a new message
     * @param type Message type
     * @param fileID File identifier
     * @param chunkNo Chunk number
     * @param chunk Chunk
     * @param replicaNo Replica number
     */
    ProtocolMessage(Type type, String fileID, Integer chunkNo, byte[] chunk, Integer replicaNo) {
        this.type = type;
        this.fileID = fileID;
        this.chunkNo = chunkNo;
        this.chunk = chunk;
        this.replicaNo = replicaNo;
    }

    /**
     * Creates a new message
     * @param type Message type
     * @param fileID File identifier
     * @param chunkNo Chunk number
     */
    ProtocolMessage(Type type, String fileID, Integer chunkNo) {
        this.type = type;
        this.fileID = fileID;
        this.chunkNo = chunkNo;
    }

    /**
     * Creates a new message
     * @param fileID File identifier
     * @param chunkNo Chunk number
     * @param replicaNo Replica number
     */
    ProtocolMessage(Type type, String fileID, Integer chunkNo, Integer replicaNo) {
        this.type = type;
        this.fileID = fileID;
        this.chunkNo = chunkNo;
        this.replicaNo = replicaNo;
    }

    /**
     * Creates a new message
     * @param type Message type
     * @param chunk Chunk
     */
    ProtocolMessage(Type type, byte[] chunk) {
        this.type = type;
        this.chunk = chunk;
    }
}
