package chord;

import java.net.InetSocketAddress;

public class Chord {

    // m must be large enough to make the probability of collisions low (2^m nodes)
    static Integer m;
    static InetSocketAddress supernode;

    static {
        m = 8;
        supernode = new InetSocketAddress("localhost", 8000);
    }
}
