package chord;

import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
    Chord settings
 */
public class Chord {

    /** Identifier space's bits (up to 2^m nodes) */
    static Integer m;

    /** Number of successor to maintain */
    static Integer r;

    /** Supernode's address */
    static InetSocketAddress supernode;

    /** Chord's executor */
    static ScheduledExecutorService executor;

    static {
        m = 4;
        r = 4;
        supernode = new InetSocketAddress("localhost", 8000);
        executor = Executors.newScheduledThreadPool(4);
    }

    private Chord() {}

    public static Integer hashToKey(String hash) {
        return new BigInteger(hash, 16).mod(new BigInteger("2").pow(m)).intValue();
    }
}
