package chord;

import java.io.Serializable;
import java.net.InetSocketAddress;

/**
 * Node's info
 */
public class ChordInfo implements Serializable {

    /** Identifier */
    public Integer identifier;

    /** ChordHandler's address */
    public InetSocketAddress chordAddress;

    /** Protocol's address */
    public InetSocketAddress protocolAddress;

    /**
     * Creates a new node's info
     * @param id Identifier
     * @param chordAddr ChordHandler's address
     * @param protocolAddr Protocol's address
     */
    ChordInfo(Integer id, InetSocketAddress chordAddr, InetSocketAddress protocolAddr) {
        identifier = id;
        chordAddress = chordAddr;
        protocolAddress = protocolAddr;
    }

    @Override
    public String toString() {
        return identifier + " - " + chordAddress + " - " + protocolAddress;
    }
}
