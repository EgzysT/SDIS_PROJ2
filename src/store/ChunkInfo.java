package store;

import java.io.Serializable;
import java.util.HashSet;

/**
 * Chunk info
 */
public class ChunkInfo implements Serializable {

    /** Size */
    public Integer size;

    /** Peers that store this chunk */
    public HashSet<Integer> peers;

    public ChunkInfo() {
        peers = new HashSet<>();
    }
}
