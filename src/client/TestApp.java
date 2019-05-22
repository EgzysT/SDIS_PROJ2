package client;

import peer.PeerService;
import utils.Logger;

import java.rmi.Naming;
import java.rmi.NotBoundException;

public class TestApp {

    private void processRequest(String accessPoint, String protocol, String filePath) {

        try {
            PeerService service = (PeerService) Naming.lookup(accessPoint);

            switch (protocol) {
                case "BACKUP":
                    service.backup(filePath);
                    break;
                case "RESTORE":
                    service.restore(filePath);
                    break;
                case "DELETE":
                    service.delete(filePath);
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
            System.out.println("Usage: java client.TestApp <accessPoint> <protocol> <filePath>");
            return;
        }

        new TestApp().processRequest(args[0], args[1], args[2]);
    }
}