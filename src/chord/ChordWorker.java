package chord;

import core.Worker;

/**
 * Chord's worker
 */
class ChordWorker extends Worker {

    /** Connection */
    private ChordConnection connection;

    /**
     * Creates a new chord's worker
     * @param connection Connection
     */
    ChordWorker(ChordConnection connection) {
        this.connection = connection;
    }

    @Override
    protected void work() {

        ChordMessage request = connection.listen();

        if (request == null)
            connection.reply(null);

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
            case GET_PREDECESSOR:
                connection.reply(ChordNode.instance().predecessor());
                break;
            case NOTIFY:
                ChordNode.instance().notify(request.node);
                break;
            case DEBUG:
                System.out.println("\n--- DEBUG ---");
                System.out.println(ChordNode.instance());
                System.out.println("-------------\n");
                break;
            case ALIVE:
            case NODE:
                break;
        }
    }
}
