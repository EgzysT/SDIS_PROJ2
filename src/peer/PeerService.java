package peer;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Peer's service to clients
 */
public interface PeerService extends Remote {

    /**
     * Backups a file
     * @param filePath File path
     * @throws RemoteException
     */
    void backup(String filePath) throws RemoteException;

    void restore(String filePath) throws RemoteException;

    void delete(String filePath) throws RemoteException;
}