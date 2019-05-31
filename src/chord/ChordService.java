package chord;

import java.math.BigInteger;
import java.net.InetSocketAddress;

/**
 * ChordHandler's interface for peer
 */
public interface ChordService {

    /**
     * Creates a new chord ring with a super node
     * @param nodeAddr Node's address
     * @param superAddr Super node's address
     */
    void join(InetSocketAddress nodeAddr, InetSocketAddress superAddr);

    /**
     * Starts Chord's service
     */
    void startService();

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
    boolean put(String fileID, Integer chunkNo, byte[] chunk);

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
    boolean remove(String fileID, Integer chunkNo);
}
