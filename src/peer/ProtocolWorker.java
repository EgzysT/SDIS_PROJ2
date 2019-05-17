package peer;

import core.ProtocolMessage;

/**
 * ProtocolWorker
 */
public class ProtocolWorker extends Worker {

	/** Connection */
	private ProtocolConnection connection;

	/**
	 * Creates a new worker
	 * 
	 * @param connection Connection
	 */
	ProtocolWorker(ProtocolConnection connection) {
		this.connection = connection;
	}

	@Override
	protected void work() {
		ProtocolMessage request = connection.listen();

		if (request == null) {
			// TODO: ignore?
		}

		PrtclMsgType type = request.getType();
		//TODO: finish switch
		switch (type) {
		case PrtclMsgType.BACKUP:
			break;
		case PrtclMsgType.RESTORE:
			break;
		case PrtclMsgType.DELETE:
			break;
		case PrtclMsgType.CHUNKDELETED:
			break;
		default:
			// Error
			break;
		}
	}
}