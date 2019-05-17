package core;

import java.io.Serializable;

public class ProtocolMessage extends Message {

    enum Type {
        BACKUP, STORED, REJECTED, RESTORE, CHUNK, DELETE, CHUNKDELETED
    }

    private Type type;
    private int senderID;
    private String fileID;
    private int chunkNo;
    private byte[] body;

    /**
     * Message Contructor for BACKUP and CHUNK messages
     * @param m_type the type of the message
     * @param id id of the node sending the message
     * @param m_fileID the fileID of the chunk
     * @param chunkNumber the chunk number
     * @param m_body the chunk's content
     */
    public Message(Type m_type, int id, String m_fileID, int chunkNumber, byte[] m_body) {
        if (m_type != Type.BACKUP && m_type != Type.CHUNK) {
            throw new Error("Creating Message With the wrong Arguments");
        }
        type = m_type;
        senderID = id;
        fileID = m_fileID;
        chunkNo = chunkNumber;
        body = m_body;
    }

    /**
     * Message constructor for types OTHER than BACKUP or CHUNK
     * @param m_type the type of the message
     * @param id id of the node sending the message
     * @param m_fileID the fileID of the chunk
     * @param chunkNumber the chunk number
     */
    public Message(Type m_type, int id, String m_fileID, int chunkNumber) {
        if (m_type == Type.BACKUP || m_type == Type.CHUNK) {
            throw new Error("Creating Message With the wrong Arguments");
        }
        type = m_type;
        senderID = id;
        fileID = m_fileID;
        chunkNo = chunkNumber;
        body = null;
    }
}
