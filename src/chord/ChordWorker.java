package chord;

import core.Worker;

class ChordWorker extends Worker {

    private ChordConnection connection;

    ChordWorker(ChordConnection connection) {
        this.connection = connection;
    }

    @Override
    protected void work() {

        ChordMessage request;

        try {
            do {
                request = connection.receive();
                System.out.println(request);

                switch (request.type) {
                    case QUERY_SUCCESSOR:
                        ChordRequestQUERY querySuccessor = (ChordRequestQUERY) request;
                        NodeInfo successor = ChordNode.instance().findSuccessor(querySuccessor.key);
                        connection.send(new ChordReplyQUERY(ChordMessage.MessageType.SUCESSOR, successor));
                        break;
                    case QUERY_PREDECESSOR:
                        ChordRequestQUERY queryPredecessor = (ChordRequestQUERY) request;
                        NodeInfo predecessor = ChordNode.instance().findPredecessor(queryPredecessor.key);
                        connection.send(new ChordReplyQUERY(ChordMessage.MessageType.PREDECESSOR, predecessor));
                        break;
                }
            } while (request.type != ChordMessage.MessageType.END);

            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
