package common.chord;

import chord.ChordInfo;
import chord.ChordNode;
import common.Worker;
import utils.Logger;

/**
 * Chord's worker
 */
class ChordWorker extends Worker {

    /** Connection */
    private ChordConnection connection;

    /**
     * Creates a new chord's worker
     * @param connection core
     */
    ChordWorker(ChordConnection connection) {
        this.connection = connection;
    }

    @Override
    protected void work() {

        ChordMessage request = connection.listen();

        if (request == null) {
            connection.reply((ChordInfo) null);
            return;
        }

        switch (request.type) {
            case CLOSEST_PRECEDING_NODE:
                connection.reply(ChordNode.instance().closestPrecedingNode(request.key));
                break;
            case FIND_SUCCESSOR:
                connection.reply(ChordNode.instance().findSuccessor(request.key));
                break;
            case GET_SUCCESSOR:
                connection.reply(ChordNode.instance().successor());
                break;
            case GET_SUCCESSORS:
                connection.reply(ChordNode.instance().successors());
                break;
            case GET_PREDECESSOR:
                connection.reply(ChordNode.instance().predecessor());
                break;
            case NOTIFY:
                ChordNode.instance().notify(request.node);
                break;
            case ALIVE:
                break;
            default:
                Logger.severe("Chord", "invalid request received");
        }
    }
}
