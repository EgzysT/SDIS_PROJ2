package core;

import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Message
 */
public class Message extends Connection implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public Message(Socket socket) {
		super(socket);
	}

	public Message(InetSocketAddress addr) {
		super(addr);
	}
	
}