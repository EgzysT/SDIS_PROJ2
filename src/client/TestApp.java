package client;

import peer.PeerService;
import utils.Logger;

import java.rmi.Naming;
import java.rmi.NotBoundException;

public class TestApp {

    private void processRequest(String accessPoint, String protocol, String arg) {

        try {
            PeerService service = (PeerService) Naming.lookup(accessPoint);

            switch (protocol) {
                case "BACKUP":
                    service.backup(arg);
                    break;
                case "RESTORE":
                    service.restore(arg);
                    break;
                case "DELETE":
                    service.delete(arg);
                    break;
                case "RECLAIM":
                    service.reclaim(Integer.parseInt(arg));
                    break;
                default:
                    System.out.println("Protocol not found.");
                    break;
            }
        } catch (NotBoundException e) {
            Logger.severe("Client", "no peer at " + accessPoint);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public static void main(String[] args) {

        if (args.length < 2) {
            System.out.println("Usage: java client.TestApp <accessPoint> <protocol> [<filePath> | <maxSize>]");
            return;
        }

        new TestApp().processRequest(args[0], args[1], args[2]);
    }
}