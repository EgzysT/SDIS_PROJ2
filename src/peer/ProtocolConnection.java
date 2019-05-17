package peer;

import java.net.InetSocketAddress;

/**
 * ProtocolConnection
 */
public class ProtocolConnection extends Connection {

	public ProtocolConnection(Socket socket) {
		super(socket);
	}

	public ProtocolConnection(InetSocketAddress addr) {
		super(addr);
	}
	
}