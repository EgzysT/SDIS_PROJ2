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

    public Boolean backupChunk(String fileID, Integer chunkNo, byte[] chunk) {

        if (client == null)
            return null;

        Boolean status;

        try {
            send(new ProtocolMessage(BACKUP, fileID, chunkNo, chunk));
            status = ((ProtocolMessage) receive()).type == ACK;
            client.close();
        } catch (IOException e) {
            return null;
        }

        return status;
    }

    // Check if node has chunk
    public Boolean checkChunk(String fileID, Integer chunkNo) {
        return true;

    }

    public byte[] restoreChunk(String fileID, Integer chunkNo) {

        if (client == null)
            return null;

        byte[] chunk;

        try {
            send(new ProtocolMessage(RESTORE, fileID, chunkNo));
            chunk = ((ProtocolMessage) receive()).chunk;
            client.close();
        } catch (IOException e) {
            return null;
        }

        return chunk;
    }

   public Boolean deleteChunk(String fileID, Integer chunkNo) {

       if (client == null)
           return null;

       try {
           send(new ProtocolMessage(DELETE, fileID, chunkNo));
           ProtocolMessage reply = (ProtocolMessage) receive();
           client.close();
           return true;
       } catch(IOException e) {
           Logger.warning("idk", "pls3");
       }

       return false;
   }

    /**
     * Listen to requests
     * @return Request received
     */
    ProtocolMessage listen() {

        if (client == null)
            return null;

        ProtocolMessage message = null;

        try {
            message = (ProtocolMessage) receive();
        } catch (IOException e) {
            Logger.warning("ChordHandler", "failed to listen to request");
        }

        return message;
    }

    /**
     * Sends a reply to request
     */
    void reply(Boolean success) {

        if (client == null)
            return;

        try {
            if (success)
                send(new ProtocolMessage(ACK));
            else
                send(new ProtocolMessage(NACK));
            client.close();
        } catch (IOException e) {
            Logger.warning("ChordHandler", "failed to reply to request");
        }
    }

    void reply(byte[] chunk) {

        if (client == null)
            return;

        try {
            if (chunk != null)
                send(new ProtocolMessage(ACK, chunk));
            else
                send(new ProtocolMessage(NACK));
            client.close();
        } catch (IOException e) {
            Logger.warning("ChordHandler", "failed to reply to request");
        }
    }
}
