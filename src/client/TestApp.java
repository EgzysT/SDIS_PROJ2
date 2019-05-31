package client;

import peer.PeerService;
import utils.Logger;

import java.rmi.Naming;

/**
 * Test App
 */
public class TestApp {

    /**
     * Processes a request
     * @param accessPoint Access point
     * @param protocol Request's protocol
     * @param arg Protocol's argument
     */
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
                case "CHORD_STATE":
                    service.chord_state();
                    break;
                case "STORE_STATE":
                    service.store_state();
                    break;
                default:
                    Logger.severe("TestApp", "protocol not found");
            }
        } catch (Exception e) {
            Logger.severe("TestApp", accessPoint + " not found");
        }
    }

    /**
     * Processes a request
     * @param accessPoint Access point
     * @param protocol Request's protocol
     */
    private void processRequest(String accessPoint, String protocol) {

        try {
            PeerService service = (PeerService) Naming.lookup(accessPoint);

            switch (protocol) {
                case "CHORD_STATE":
                    service.chord_state();
                    break;
                case "STORE_STATE":
                    service.store_state();
                    break;
                default:
                    Logger.severe("TestApp", "protocol not found");
            }
        } catch (Exception e) {
            Logger.severe("TestApp", accessPoint + " not found");
        }
    }

    public static void main(String[] args) {

        if (args.length < 2) {
            Logger.info("TestApp", "java client.TestApp <accessPoint> <protocol> [<filePath> | <maxSize>]");
            return;
        }

        if (args.length > 2)
            new TestApp().processRequest(args[0], args[1], args[2]);
        else
            new TestApp().processRequest(args[0], args[1]);
    }
}