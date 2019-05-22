package chord;

import java.math.BigInteger;
import java.net.InetSocketAddress;

/**
 * ChordHandler's interface for peer
 */
public interface ChordService {

    /**
     * Creates a super node
     */
    void createSuperNode();

    /**
     * Creates a node
     * @param addr Address
     */
    void createNode(InetSocketAddress addr);

    /**
     * Returns node's identifier
     * @return Node's identifier
     */
    BigInteger id();

    /**
     * Puts a value in the DHT
     * @param fileID File identifier
     * @param chunkNo Chunk number
     * @param chunk Chunk data
     */
    void put(String fileID, Integer chunkNo, byte[] chunk);

    /**
     * Gets a value from the DHT
     * @param fileID File identifier
     * @param chunkNo Chunk number
     * @return Value from DHT if key exists, null otherwise
     */
    byte[] get(String fileID, Integer chunkNo);

    /**
     * Removes a value from the DHT
     * @param fileID File identifier
     * @param chunkNo Chunk number
     */
    void remove(String fileID, Integer chunkNo);

//    HashSet<InetSocketAddress> backupPeers(Integer key);
}
