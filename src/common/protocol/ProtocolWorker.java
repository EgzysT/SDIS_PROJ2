package common.protocol;

import common.Worker;
import protocol.Protocol;
import protocol.ProtocolHandler;
import store.Store;
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
            case BACKUP_CHUNK:
                Protocol.writeChunk(connection, request.fileID, request.chunkNo, request.chunk, request.replicaNo);
                break;
            case RESTORE_CHUNK:
                Protocol.readChunk(connection, request.fileID, request.chunkNo);
                break;
            case DELETE_CHUNK:
                connection.reply(Protocol.deleteChunk(request.fileID, request.chunkNo, -1));
                break;
            case HAS_CHUNK:
                connection.reply(Store.hasChunkReplica(request.fileID, request.chunkNo, request.replicaNo));
                break;
            default:
                Logger.severe("Protocol", "invalid request received");
        }
    }
}
