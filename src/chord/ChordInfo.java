package chord;

import java.io.Serializable;
import java.math.BigInteger;
import java.net.InetSocketAddress;

/**
 * Chord node's info
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

    /**
     * Sets selected port
     * @param port Port selected by OS
     */
    public void setProtocolPort(Integer port) {
        protocolAddress = new InetSocketAddress(chordAddress.getHostName(), port);
    }

    @Override
    public String toString() {
        return identifier + " - " + chordAddress.getAddress().getHostAddress() + ":" + chordAddress.getPort();
    }

    @Override
    public boolean equals(Object obj) {
        ChordInfo node = (ChordInfo) obj;
        return chordAddress.equals(node.chordAddress) && chordAddress.getPort() == node.chordAddress.getPort();
    }
}
