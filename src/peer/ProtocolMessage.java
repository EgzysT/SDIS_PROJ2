package core;

import java.io.Serializable;

public class ProtocolMessage extends Message {

    /**
     * BACKUP       - Sends backup request with chunk in body.
     * STORED       - Reply to BACKUP saying that chunk was stored successfully.
     * REJECTED     - Reply to BACKUP saying that chunk was NOT stored due to space limitations.
     * RESTORE      - Sends restore chunk request.
     * CHUNK        - Reply to RESTORE with the chunk in the body.
     * NOTFOUND     - Reply to RESTORE saying that chunk does not exist at the target node.
     * DELETE       - Sends delete chunk request.
     * CHUNKDELETED - Sends information to original node saying that his chunk was deleted. //TODO: Verify if this should return also the original chunk in the msg's body.
     */
    enum PrtclMsgType {
        BACKUP, STORED, REJECTED, RESTORE, CHUNK, NOTFOUND, DELETE, CHUNKDELETED
    }

    private PrtclMsgType type;
    private int senderID;
    private String fileID;
    private int chunkNo;
    private byte[] body;

    /**
     * Message Contructor for BACKUP and CHUNK messages
     * 
     * @param m_type      the type of the message
     * @param id          id of the node sending the message
     * @param m_fileID    the fileID of the chunk
     * @param chunkNumber the chunk number
     * @param m_body      the chunk's content
     */
    public ProtocolMessage(PrtclMsgType m_type, int id, String m_fileID, int chunkNumber, byte[] m_body) {
        if (m_type != Type.BACKUP && m_type != Type.CHUNK) {
            throw new Error("Creating Message With the wrong Arguments");
        }
        type = m_type;
        setSenderID(id);
        fileID = m_fileID;
        setChunkNo(chunkNumber);
        setBody(m_body);
    }

    /**
     * Message constructor for types OTHER than BACKUP or CHUNK
     * 
     * @param m_type      the type of the message
     * @param id          id of the node sending the message
     * @param m_fileID    the fileID of the chunk
     * @param chunkNumber the chunk number
     */
    public ProtocolMessage(PrtclMsgType m_type, int id, String m_fileID, int chunkNumber) {
        if (m_type == Type.BACKUP || m_type == Type.CHUNK) {
            throw new Error("Creating Message With the wrong Arguments");
        }
        type = m_type;
        setSenderID(id);
        fileID = m_fileID;
        setChunkNo(chunkNumber);
        setBody(null);
    }

    /**
     * @return the type
     */
    public PrtclMsgType getType() {
        return type;
    }

    /**
     * @return the fileID
     */
    public String getFileID() {
        return fileID;
    }

    /**
     * @return the senderID
     */
    public int getSenderID() {
        return senderID;
    }

    /**
     * @return the chunkNo
     */
    public int getChunkNo() {
        return chunkNo;
    }

    /**
     * @return the body
     */
    public byte[] getBody() {
        return body;
    }
}
