package chord;

import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
    Chord's settings
 */
public class ChordHandler {

    /** Identifier space's bits (up to 2^m nodes) */
    static Integer m;

    /** Number of successor to maintain */
    static Integer r;

    /** Super-node's address */
    static InetSocketAddress supernode;

    /** Chord's executor */
    static ScheduledExecutorService executor;

    static {
        m = 4;
        r = 4;
        supernode = new InetSocketAddress("localhost", 8000);
        executor = Executors.newScheduledThreadPool(4);
    }

    private ChordHandler() {}

    /**
     * Returns chord key from hash
     * @param hash Hash to use
     * @return Chord key associated with hash
     */
    public static Integer hashToKey(String hash) {
        return new BigInteger(hash, 16).mod(new BigInteger("2").pow(m)).intValue();
    }
}
