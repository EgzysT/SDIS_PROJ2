//package peer;
//
//import java.io.IOException;
//import java.net.InetSocketAddress;
//
//import core.core;
//import core.ProtocolMessage;
//import core.ProtocolMessage.PrtclMsgType;
//import core.ProtocolMessage.Type;
//
///**
// * ProtocolConnection
// * TODO: additional verifications in communications. (The sent messages must have the same fileID and chunkNumber as the responses, etc...)
// */
//public class ProtocolConnection extends core {
//
//	public ProtocolConnection(Socket socket) {
//		super(socket);
//	}
//
//	public ProtocolConnection(InetSocketAddress addr) {
//		super(addr);
//	}
//
//	/**
//	 * sends a chunk backup request to the other node, and awaits response.
//	 * @param chunk
//	 * @param id
//	 * @param fileID
//	 * @param chunkNumber
//	 * @return true if chunk was stored, false if it was rejected (due to space limitations), null if error.
//	 */
//	public Boolean sendChunkBackup(byte[] chunk, int id, String fileID, int chunkNumber) {
//		ProtocolMessage msg = new ProtocolMessage(PrtclMsgType.BACKUP, id, fileID, chunkNumber, chunk);
//		ProtocolMessage response = sendResponseMessage(msg);
//		if (response == null) return null;
//		else if (response.getType() == PrtclMsgType.STORED) {
//			return true;
//		}
//		else if (response.getType() == PrtclMsgType.REJECTED) {
//			return false;
//		}
//		else throw new Error("Error: Got a response to backup that wasn't STORED or REJECTED");
//	}
//
//	/**
//	 * Sends restore request to other node, awaits response.
//	 * @param id
//	 * @param fileID
//	 * @param chunkNumber
//	 * @return byte[] with the contents of the chunk if available, null otherwise (due to node offline or chunk not found).
//	 */
//	public byte[] sendRestoreRequest(int id, String fileID, int chunkNumber) {
//		ProtocolMessage msg = new ProtocolMessage(PrtclMsgType.RESTORE, id, fileID, chunkNumber);
//		ProtocolMessage response = sendResponseMessage(msg);
//		if (response == null) return null;
//		else if (response.getType() == PrtclMsgType.CHUNK) {
//			return response.getBody();
//		}
//		else if (response.getType() == PrtclMsgType.NOTFOUND) {
//			return null;
//		}
//		else throw new Error("Error: Got a response to backup that wasn't STORED or REJECTED");
//	}
//
//	/**
//	 * Sends a delete protocol request to the other node.
//	 * @param id
//	 * @param fileID
//	 * @param chunkNumber
//	 * @return false in case of failure, true otherwise.
//	 */
//	public boolean sendDelete(int id, String fileID, int chunkNumber) {
//		ProtocolMessage msg = new ProtocolMessage(PrtclMsgType.DELETE, id, fileID, chunkNumber);
//		return sendSimpleMessage(msg);
//	}
//
//	/**
//	 * Sends a warning to the other node saying that the chunk was deleted in space reclaiming.
//	 * @param id
//	 * @param fileID
//	 * @param chunkNumber
//	 * @return false in case of failure, true otherwise.
//	 */
//	public boolean sendChunkDeleted(int id, String fileID, int chunkNumber) {
//		ProtocolMessage msg = new ProtocolMessage(PrtclMsgType.CHUNKDELETED, id, fileID, chunkNumber);
//		return sendSimpleMessage(msg);
//	}
//
//	/**
//	 * Sends STORED
//	 * @param id
//	 * @param fileID
//	 * @param chunkNumber
//	 * @return
//	 */
//	public boolean sendStored(int id, String fileID, int chunkNumber) {
//		ProtocolMessage msg = new ProtocolMessage(PrtclMsgType.STORED, id, fileID, chunkNumber);
//		return sendSimpleMessage(msg);
//	}
//
//	/**
//	 * Sends REJECTED
//	 * @param id
//	 * @param fileID
//	 * @param chunkNumber
//	 * @return
//	 */
//	public boolean sendRejected(int id, String fileID, int chunkNumber) {
//		ProtocolMessage msg = new ProtocolMessage(PrtclMsgType.REJECTED, id, fileID, chunkNumber);
//		return sendSimpleMessage(msg);
//	}
//
//	/**
//	 * Sends CHUNK
//	 * @param id
//	 * @param fileID
//	 * @param chunkNumber
//	 * @param body
//	 * @return
//	 */
//	public boolean sendChunk(int id, String fileID, int chunkNumber, byte[] body) {
//		ProtocolMessage msg = new ProtocolMessage(PrtclMsgType.CHUNK, id, fileID, chunkNumber, body);
//		return sendSimpleMessage(msg);
//	}
//
//	/**
//	 * Sends NOTFOUND
//	 * @param id
//	 * @param fileID
//	 * @param chunkNumber
//	 * @return
//	 */
//	public boolean sendChunkNotFound(int id, String fileID, int chunkNumber) {
//		ProtocolMessage msg = new ProtocolMessage(PrtclMsgType.NOTFOUND, id, fileID, chunkNumber, body);
//		return sendSimpleMessage(msg);
//	}
//
//	/**
//	 * Helper function to send messages that REQUIRE responses. Returnw the message.
//	 * @param msg
//	 * @return the response message
//	 */
//	private ProtocolMessage sendResponseMessage(ProtocolMessage msg) {
//		if (client == null) {
//			return null;
//		}
//		try {
//			send(msg);
//			ProtocolMessage response = (ProtocolMessage) receive();
//			close();
//			return response;
//		} catch (IOException e) {
//			return null;
//		}
//	}
//
//	/**
//	 * Helper function to send messages that do not require responses. Returns boolean
//	 * @param msg
//	 * @return true if message sent successfully, false otherwise.
//	 */
//	private boolean sendSimpleMessage(ProtocolMessage msg) {
//		if (client == null) {
//			return false;
//		}
//
//		try {
//			send(msg);
//			close();
//			return true;
//		} catch (IOException e) {
//			return false;
//		}
//	}
//
//
//	/**
//     * Listen to requests
//     * @return Request received
//     */
//    public ProtocolMessage listen() {
//
//        if (client == null)
//            return null;
//
//		ProtocolMessage msg = null;
//
//        try {
//            msg = (ProtocolMessage) receive();
//        } catch (IOException e) {
//            Logger.warning("Protocol", "failed to listen to request");
//        }
//
//        return msg;
//    }
//}