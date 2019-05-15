package chord;

import core.Worker;
import sun.rmi.runtime.Log;
import utils.Logger;

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
                Logger.info("In", message.toString());

                switch (message.type) {
                    case CLOSEST_PRECEDING_NODE: {
                        ChordMessageKey request = (ChordMessageKey) message;
                        NodeInfo cpf = ChordNode.instance().closestPrecedingNode(request.key);
                        connection.send(new ChordMessageNode(cpf).type(ChordMessage.MessageType.NODE));
                        Logger.info("Out", cpf.toString());
                        break;
                    }
                    case FIND_SUCCESSOR: {
                        ChordMessageKey request = (ChordMessageKey) message;
                        NodeInfo successor = ChordNode.instance().findSuccessor(request.key);
                        connection.send(new ChordMessageNode(successor).type(ChordMessage.MessageType.NODE));
                        Logger.info("Out", successor.toString());
                        break;
                    }
//                    case FIND_PREDECESSOR: {
//                        ChordMessageKey request = (ChordMessageKey) message;
//                        NodeInfo predecessor = ChordNode.instance().findPredecessor(request.key);
//                        connection.send(new ChordMessageNode(predecessor).type(ChordMessage.MessageType.NODE));
//                        Logger.info("Out", predecessor.toString());
//                        break;
//                    }
                    case GET_SUCCESSOR: {
                        NodeInfo successor = ChordNode.instance().successor();
                        connection.send(new ChordMessageNode(successor).type(ChordMessage.MessageType.NODE));
                        Logger.info("Out", successor.toString());
                        break;
                    }
                    case GET_PREDECESSOR: {
                        NodeInfo predecessor = ChordNode.instance().predecessor();
                        connection.send(new ChordMessageNode(predecessor).type(ChordMessage.MessageType.NODE));
                        Logger.info("Out", predecessor.toString());
                        break;
                    }
                    case SET_PREDECESSOR: {
                        ChordMessageNode request = (ChordMessageNode) message;
                        ChordNode.instance().predecessor(request.info);
                        connection.send(new ChordMessage().type(ChordMessage.MessageType.OK));
                        Logger.info("Out", "ok");
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
