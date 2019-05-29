package common.protocol;

import common.Worker;
import protocol.Protocol;
import utils.Logger;

public class ProtocolWorker extends Worker {

    private ProtocolConnection connection;

    ProtocolWorker(ProtocolConnection connection) {
        this.connection = connection;
    }

    @Override
    public void work() {

        ProtocolMessage request = connection.listen();

        if (request == null)
            return;

        switch (request.type) {
            case BACKUP:
                Protocol.writeChunk(connection, request.fileID, request.chunkNo, request.chunk, request.i);
                return;
            case RESTORE:
                Protocol.readChunk(connection, request.fileID, request.chunkNo);
                return;
            case DELETE:
                connection.reply(Protocol.deleteChunk(request.fileID, request.chunkNo, -1));
                return;
            default:
                Logger.severe("Protocol", "invalid request received");
        }

        connection.reply(false);
    }
}
