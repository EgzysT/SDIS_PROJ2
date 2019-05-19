package peer;

import chord.ChordNode;
import protocol.Backup;
import protocol.ProtocolHandler;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.atomic.AtomicInteger;

public class Peer extends UnicastRemoteObject implements Service {

    private static Peer instance;
    private static AtomicInteger currentSpace, maxSpace;

    private Peer() throws RemoteException {
        super(0);

        currentSpace = new AtomicInteger(0);
        maxSpace = new AtomicInteger(Integer.MAX_VALUE);
    }

    public static Peer instance() {
        if (instance == null) {
            try {
                instance = new Peer();
            } catch (RemoteException e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }

        return instance;
    }

    public void init(String accessPoint) {

        // Initiate chord node
        ChordNode.instance().initSuperNode();

        try {
            Naming.rebind(accessPoint, this);



        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

    }

    @Override
    public void backup(String filePath) {
//        ProtocolHandler.schedule(
//                () -> new Backup,
//                0
//        );
    }



    public static void main(String[] args) throws Exception {

        Peer.instance().init("peer1");


        Service service = (Service) Naming.lookup("peer1");
        service.backup("haha");





    }
}
