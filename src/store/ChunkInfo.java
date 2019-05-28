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
    public HashSet<Integer> replicas;

    public ChunkInfo(Integer size) {
        this.size = size;
        this.replicas = new HashSet<>();
    }
}
