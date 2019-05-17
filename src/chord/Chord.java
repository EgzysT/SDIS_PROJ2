package chord;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
    @brief Chord settings
 */
class Chord {

    /** Identifier space's bits (up to 2^m nodes) */
    static Integer m;

    /** Supernode's address */
    static InetSocketAddress supernode;

    /** Chord's executor */
    static ScheduledExecutorService executor;

    static {
        m = 4;
        supernode = new InetSocketAddress("localhost", 8000);
        executor = Executors.newScheduledThreadPool(2);
    }

    private Chord() {}
}
