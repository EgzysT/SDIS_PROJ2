package common.protocol;

import common.Connection;
import utils.Logger;

import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.InetSocketAddress;

import static common.protocol.ProtocolMessage.Type.*;

public class ProtocolConnection extends Connection {

    public ProtocolConnection(SSLSocket socket) {
        super(socket);
    }

    public ProtocolConnection(InetSocketAddress addr) {
        super(addr);
    }

    /**
     * Requests backup of a chunk.
     * @param fileID File identifier
     * @param chunkNo Chunk number
     * @param chunk Chunk
     * @param replicaNo Replica number
     * @return Reply
     */
    public ProtocolMessage backupChunk(String fileID, Integer chunkNo, byte[] chunk, Integer replicaNo) {

        if (client == null)
            return null;

        ProtocolMessage reply;

        try {
            send(new ProtocolMessage(BACKUP, fileID, chunkNo, chunk, replicaNo));
            reply = ((ProtocolMessage) receive());
            client.close();
        } catch (IOException e) {
            return null;
        }

        return reply;
    }

    /**
     * Requests restore of a chunk.
     * @param fileID File identifier
     * @param chunkNo Chunk number
     * @return Reply
     */
    public ProtocolMessage restoreChunk(String fileID, Integer chunkNo) {

        if (client == null)
            return null;

        ProtocolMessage reply;

        try {
            send(new ProtocolMessage(RESTORE, fileID, chunkNo));
            reply = ((ProtocolMessage) receive());
            client.close();
        } catch (IOException e) {
            return null;
        }

        return reply;
    }

    /**
     * Requests deletion of a chunk.
     * @param fileID File identifier
     * @param chunkNo Chunk number
     * @return Reply
     */
   public ProtocolMessage deleteChunk(String fileID, Integer chunkNo) {

       if (client == null)
           return null;

       ProtocolMessage reply;

       try {
           send(new ProtocolMessage(DELETE, fileID, chunkNo));
           reply = (ProtocolMessage) receive();
           client.close();
       } catch(IOException e) {
           return null;
       }

       return reply;
   }

    /**
     * Listen to requests.
     * @return Request received
     */
    public ProtocolMessage listen() {

        if (client == null)
            return null;

        ProtocolMessage message = null;

        try {
            message = (ProtocolMessage) receive();
        } catch (IOException e) {
            Logger.warning("Chord", "failed to listen to request");
        }

        return message;
    }

    /**
     * Sends a reply to request.
     */
    public void reply(Boolean success) {

        if (client == null)
            return;

        try {
            if (success)
                send(new ProtocolMessage(ACK));
            else
                send(new ProtocolMessage(NACK));

            client.close();
        } catch (IOException e) {
            Logger.warning("Chord", "failed to reply to request");
        }
    }

    /**
     * Sends a reply to a request.
     * @param chunk Chunk
     */
    public void reply(byte[] chunk) {

        if (client == null)
            return;

        try {
            if (chunk != null)
                send(new ProtocolMessage(ACK, chunk));
            else
                send(new ProtocolMessage(NACK));

            client.close();
        } catch (IOException e) {
            Logger.warning("Chord", "failed to reply to request");
        }
    }
}
