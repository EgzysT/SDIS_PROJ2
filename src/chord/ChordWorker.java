package chord;

import core.Worker;

class ChordWorker extends Worker {

    private ChordConnection connection;

    ChordWorker(ChordConnection connection) {
        this.connection = connection;
    }

    @Override
    protected void work() {

        ChordMessage message;

        try {
            do {
                message = connection.receive();
                System.out.println(message);

                switch (message.type) {
                    case CLOSEST_PRECEDING_FINGER: {
                        ChordMessageKey request = (ChordMessageKey) message;
                        NodeInfo cpf = ChordNode.instance().closestPrecedingFinger(request.key);
                        connection.send(new ChordMessageNode(cpf).type(ChordMessage.MessageType.NODE));
                        break;
                    }
                    case FIND_SUCCESSOR: {
                        ChordMessageKey request = (ChordMessageKey) message;
                        NodeInfo successor = ChordNode.instance().findSuccessor(request.key);
                        connection.send(new ChordMessageNode(successor).type(ChordMessage.MessageType.NODE));
                        break;
                    }
                    case FIND_PREDECESSOR: {
                        ChordMessageKey request = (ChordMessageKey) message;
                        NodeInfo successor = ChordNode.instance().findPredecessor(request.key);
                        connection.send(new ChordMessageNode(successor).type(ChordMessage.MessageType.NODE));
                        break;
                    }
                    case GET_SUCCESSOR: {
                        NodeInfo successor = ChordNode.instance().successor();
                        connection.send(new ChordMessageNode(successor).type(ChordMessage.MessageType.NODE));
                        break;
                    }
                    case GET_PREDECESSOR: {
                        NodeInfo successor = ChordNode.instance().predecessor();
                        connection.send(new ChordMessageNode(successor).type(ChordMessage.MessageType.NODE));
                        break;
                    }
                    case SET_PREDECESSOR: {
                        ChordMessageNode request = (ChordMessageNode) message;
                        ChordNode.instance().predecessor(request.info);
                        connection.send(new ChordMessage().type(ChordMessage.MessageType.OK));
                        break;
                    }
                    case DEBUG: {
                        System.out.println("\n--- DEBUG ---");
                        System.out.println(ChordNode.instance());
                        System.out.println("-------------\n");
                        break;
                    }
                }
            } while (message.type != ChordMessage.MessageType.END);

            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
