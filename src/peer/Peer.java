package peer;

import chord.ChordNode;
import protocol.Backup;
import protocol.Delete;
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

    private void start(String accessPoint, InetSocketAddress addr) {

        ChordNode.instance().join(addr);

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

        try {
            Naming.rebind(accessPoint, this);
        } catch (Exception e) {
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
       ProtocolHandler.schedule(
               () -> Delete.deleteFile(filePath),
               0
       );
    }

    public static void main(String[] args) throws Exception {

        String sslDir = System.getProperty("user.dir") + File.separator + "ssl" + File.separator;

        System.setProperty("javax.net.ssl.keyStore", sslDir + "keystore.keys");
        System.setProperty("javax.net.ssl.keyStorePassword", "123456");

        System.setProperty("javax.net.ssl.trustStore", sslDir + "truststore");
        System.setProperty("javax.net.ssl.trustStorePassword", "123456");

        if (args.length < 2) {
            System.out.println("Usage: java peer.Peer <accessPoint> <host:port>");
            System.exit(-1);
        }

        String[] tmp = args[1].split(":");

        Peer.instance().start(args[0], new InetSocketAddress(tmp[0], Integer.parseInt(tmp[1])));
    }
}
