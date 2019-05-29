package chord;

import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

    /** Replication degree */
    static Integer repDeg;

    /** Super-node's address */
    static InetSocketAddress supernode;

    /** Chord's executor */
    static ScheduledExecutorService executor;

    static {
        m = 32;
        r = 10;
        repDeg = 5;
        executor = Executors.newScheduledThreadPool(2);
    }

    private ChordHandler() {}

    /**
     * Returns chord key from hash
     * @param hash Hash to use
     * @return Chord key associated with hash
     */
    public static BigInteger hashToKey(String hash, Integer replica) {

        StringBuilder keyHash = new StringBuilder();

        try {
            byte[] h = MessageDigest.getInstance("SHA-1").digest((replica + hash).getBytes());

            for (byte b : h) {
                keyHash.append(Integer.toHexString(0xFF & b));
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        return new BigInteger(keyHash.toString(), 16).mod(new BigInteger("2").pow(m));
    }
}
