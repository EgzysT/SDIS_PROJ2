package chord;

import utils.Logger;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

class Chord {

    // m must be large enough to make the probability of collisions low (2^m nodes)
    static Integer m;
    static InetSocketAddress supernode;
    static ScheduledExecutorService executor;

    static {
        m = 4;
        supernode = new InetSocketAddress("localhost", 8000);
        executor = Executors.newScheduledThreadPool(2);
    }
}
