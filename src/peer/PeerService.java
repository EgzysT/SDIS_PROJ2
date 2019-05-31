package peer;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Peer's service to clients
 */
public interface PeerService extends Remote {

    /**
     * Backups a file.
     * @param filePath File path
     * @throws RemoteException
     */
    void backup(String filePath) throws RemoteException;

    /**
     * Restores a previously backed-up file.
     * @param filePath File path
     * @throws RemoteException
     */
    void restore(String filePath) throws RemoteException;

    /**
     * Deletes a file's backup.
     * @param filePath File path
     * @throws RemoteException
     */
    void delete(String filePath) throws RemoteException;

     /**
      * Sets a maximum disk space to be used.
      * @param maxSize Max disk space
      * @throws RemoteException
      */
     void reclaim(Integer maxSize) throws RemoteException;

    /**
     * Shows Chord state
     * @throws RemoteException
     */
     void chord_state() throws RemoteException;

    /**
     * Shows store state
     * @throws RemoteException
     */
     void store_state() throws RemoteException;
}
