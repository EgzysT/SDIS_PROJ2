package store;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;

/**
 * Chunk info
 */
public class ChunkInfo implements Serializable {

    /** Size */
    public Integer size;

    /** Replicas */
    public HashSet<Integer> replicas;

    /**
     * Chunk info
     * @param size Size
     */
    public ChunkInfo(Integer size) {
        this.size = size;
        this.replicas = new HashSet<>();
    }
}
