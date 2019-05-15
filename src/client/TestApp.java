//package client;
//
//import java.rmi.Naming;
//import java.rmi.NotBoundException;
//import java.rmi.Remote;
//import java.rmi.RemoteException;
//import java.util.Arrays;
//
//public class TestApp {
//
//    public static void main(String[] args) {
//        if (args.length < 2) {
//            System.out.println("Usage: java App <peer_ap> <sub_protocol> <opnd_1> <opnd_2>");
//            return;
//        }
//
//        new TestApp().processRequest(args[0], args[1], Arrays.copyOfRange(args, 2, args.length));
//    }
//
//    private void processRequest(String peer_ap, String protocol, String[] opnd) {
//
//        try {
//            IService service = (IService) Naming.lookup(peer_ap);
//
//            switch (protocol) {
//                case "BACKUP":
//                    service.backup(opnd[0], Integer.valueOf(opnd[1]), "1.0");
//                    break;
//                case "BACKUPENH":
//                    service.backup(opnd[0], Integer.valueOf(opnd[1]), "2.0");
//                    break;
//                case "RESTORE":
//                    service.restore(opnd[0], "1.0");
//                    break;
//                case "RESTOREENH":
//                    service.restore(opnd[0], "2.0");
//                    break;
//                case "DELETE":
//                    service.delete(opnd[0], "1.0");
//                    break;
//                case "DELETEENH":
//                    service.delete(opnd[0], "2.0");
//                    break;
//                case "RECLAIM":
//                    service.reclaim(Integer.parseInt(opnd[0]));
//                    break;
//                case "STATE":
//                    service.state();
//                    break;
//                default:
//                    System.out.println("Protocol not found.");
//                    break;
//            }
//        } catch (NotBoundException e) {
//            Logger.severe("Client: no peer at " + peer_ap + ".");
//            System.exit(-1);
//        } catch (Exception e) {
//            e.printStackTrace();
//            System.exit(-1);
//        }
//    }
//}