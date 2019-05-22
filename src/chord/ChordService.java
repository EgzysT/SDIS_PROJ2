package chord;

import java.net.InetSocketAddress;

/**
 * Chord's interface for peer
 */
public interface ChordService {

    void createSuperNode();

    void createNode(InetSocketAddress addr);


    Integer id();

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
