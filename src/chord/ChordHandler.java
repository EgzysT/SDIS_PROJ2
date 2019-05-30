package chord;

import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
    Chord's settings
 */
public abstract class ChordHandler {

    /** Identifier space's bits (up to 2^m nodes) */
    static Integer m;

    /** Number of successor to maintain */
    static Integer r;

    /** Replication degree */
    static Integer repDeg;

    /** Chord's executor */
    private static ScheduledExecutorService executor;

    static {
        m = 32;
        r = 10;
        repDeg = 5;
        executor = Executors.newScheduledThreadPool(4);
    }

    /**
     * Submits a task
     * @param task task
     */
    public static void submit(Runnable task) {
        executor.submit(task);
    }

    /**
     * Schedules a task
     * @param task Task
     * @param delay Task's delay (in milliseconds)
     */
    public static void schedule(Runnable task, Integer delay) {
        executor.schedule(
                task,
                delay,
                TimeUnit.MILLISECONDS
        );
    }

    /**
     * Returns chord key from hash
     * @param hash Hash to use
     * @return Chord key associated with hash
     */
    public static BigInteger hashToKey(String hash) {

        StringBuilder keyHash = new StringBuilder();

        try {
            byte[] h = MessageDigest.getInstance("SHA-1").digest(hash.getBytes());

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
