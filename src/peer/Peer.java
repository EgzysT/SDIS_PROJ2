package peer;

import chord.ChordNode;
import protocol.*;
import store.Store;

import java.io.File;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Peer extends UnicastRemoteObject implements PeerService {

    private static Peer instance;
    public Path homeDir, backupDir, restoreDir;

    private Peer() throws RemoteException {
        super(0);
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

    private void start(String accessPoint, InetSocketAddress nodeAddr, InetSocketAddress superAddr) {

        ChordNode.instance().join(nodeAddr, superAddr);

        homeDir = Paths.get(
                System.getProperty("user.home") + File.separator + "Desktop" +
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

        // Import store
        Store.importStore();

        ChordNode.instance().initThreads();

        try {
            Naming.rebind(accessPoint, this);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    @Override
    public void backup(String filePath) {
        ProtocolHandler.submit(
                () -> Backup.backupFile(filePath)
        );
    }

    @Override
    public void restore(String filePath) {
        ProtocolHandler.submit(
                () -> Restore.restoreFile(filePath)
        );
    }

    @Override
    public void delete(String filePath) {
       ProtocolHandler.submit(
               () -> Delete.deleteFile(filePath)
       );
    }

    @Override
    public void reclaim(Integer maxSize) {
        ProtocolHandler.submit(
                () -> Reclaim.reclaimSpace(maxSize)
        );
    }

    public static void main(String[] args) {

        String sslDir = System.getProperty("user.dir") + File.separator + "ssl" + File.separator;

        System.setProperty("javax.net.ssl.keyStore", sslDir + "keystore.keys");
        System.setProperty("javax.net.ssl.keyStorePassword", "123456");

        System.setProperty("javax.net.ssl.trustStore", sslDir + "truststore");
        System.setProperty("javax.net.ssl.trustStorePassword", "123456");

        if (args.length < 2) {
            System.out.println("Usage: java peer.Peer <accessPoint> <host:port> [<superhost:port]");
            System.exit(-1);
        }

        String[] host_port = args[1].split(":");

        InetSocketAddress host = new InetSocketAddress(host_port[0], Integer.parseInt(host_port[1]));
        InetSocketAddress superHost = null;

        if (args.length > 2) {
            String[] superhost_port = args[2].split(":");
            superHost = new InetSocketAddress(superhost_port[0], Integer.parseInt(superhost_port[1]));
        }

        Peer.instance().start(
                args[0],
                host,
                superHost
        );
    }
}
