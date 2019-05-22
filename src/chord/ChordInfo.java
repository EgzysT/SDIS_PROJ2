package chord;

import java.io.Serializable;
import java.math.BigInteger;
import java.net.InetSocketAddress;

/**
 * Node's info
 */
public class ChordInfo implements Serializable {

    /** Identifier */
    public BigInteger identifier;

    /** Chord's address */
    public InetSocketAddress chordAddress;

    /** Protocol's address */
    public InetSocketAddress protocolAddress;

    /**
     * Creates a new node's info
     * @param id Identifier
     * @param chordAddr Chord's address
     * @param protocolAddr Protocol's address
     */
    ChordInfo(BigInteger id, InetSocketAddress chordAddr, InetSocketAddress protocolAddr) {
        identifier = id;
        chordAddress = chordAddr;
        protocolAddress = protocolAddr;
    }

    public void setProtocolPort(Integer port) {
        protocolAddress = new InetSocketAddress(chordAddress.getHostName(), port);
    }

    @Override
    public String toString() {
        return identifier + " - " + chordAddress.getAddress().getHostAddress() + ":" + chordAddress.getPort();
    }
}
