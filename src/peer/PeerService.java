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

    /**
     * Restores a previously backed-up file
     * @param filePath
     * @throws RemoteException
     */
    void restore(String filePath) throws RemoteException;

    /**
     * Deletes a file's backup
     * @param filePath
     * @throws RemoteException
     */
    void delete(String filePath) throws RemoteException;

    // /**
    //  * sets a maxSize for the memory this program will use. If smaller it might delete chunks from other computers.
    //  * @param maxSize
    //  * @throws RemoteException
    //  */
    // void reclaim(int maxSize) throws RemoteException;
}
