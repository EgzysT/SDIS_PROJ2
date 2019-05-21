package peer;

import chord.ChordNode;
import protocol.Backup;
import protocol.ProtocolHandler;
import protocol.Restore;

import java.io.File;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.atomic.AtomicInteger;

public class Peer extends UnicastRemoteObject implements PeerService {

    private static Peer instance;
    public Path homeDir, backupDir, restoreDir;
    public AtomicInteger currentDiskSpace, maxDiskSpace;

    private Peer() throws RemoteException {
        super(0);

        currentDiskSpace = new AtomicInteger(0);
        maxDiskSpace = new AtomicInteger(Integer.MAX_VALUE);
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

    private void start(Boolean isSuper, String accessPoint, String host) {

        try {
            Naming.rebind(accessPoint, this);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        // Initiate chord node
        if (isSuper) {
            ChordNode.instance().createSuperNode();
        } else {
            String[] host_port = host.split(":");
            ChordNode.instance().createNode(new InetSocketAddress(host_port[0], Integer.parseInt(host_port[1])));
        }

        homeDir = Paths.get(System.getProperty("user.home") + File.separator + "Desktop" +
                File.separator + "peer" + ChordNode.instance().id() + File.separator
        );

        backupDir = Paths.get(homeDir + File.separator + "backup");
        restoreDir = Paths.get(homeDir + File.separator + "restored");

        try {
            Files.createDirectories(backupDir);
            Files.createDirectories(restoreDir);
        } catch(Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    @Override
    public void backup(String filePath) {
        ProtocolHandler.schedule(
                () -> Backup.backupFile(filePath),
                0
        );
    }

    @Override
    public void restore(String filePath) {
        ProtocolHandler.schedule(
                () -> Restore.restoreFile(filePath),
                0
        );
    }

    @Override
    public void delete(String filePath) {
//        ProtocolHandler.schedule(
//                () -> Backup.backupFile(filePath),
//                0
//        );
    }

    public static void main(String[] args) throws Exception {

        String sslDir = "/home/miguelalexbt/IdeaProjects/SDIS_PROJ2/src/ssl/";

        System.setProperty("javax.net.ssl.keyStore", sslDir + "keystore.keys");
        System.setProperty("javax.net.ssl.keyStorePassword", "123456");

        System.setProperty("javax.net.ssl.trustStore", sslDir + "truststore");
        System.setProperty("javax.net.ssl.trustStorePassword", "123456");

        // TODO change host:port to host, dynamically choose port
        if (args.length < 2) {
            System.out.println("Usage: java peer.Peer <isSuper> <accessPoint> <host:port>");
            System.exit(-1);
        }

        // For super node:
        // java peer.Peer true peerX XXX.X.X.X:XXXX

        // For node:
        // java peer.Peer false peerY YYY.Y.Y.Y:YYY

        Peer.instance().start(Boolean.parseBoolean(args[0]), args[1], args[2]);
    }
}
