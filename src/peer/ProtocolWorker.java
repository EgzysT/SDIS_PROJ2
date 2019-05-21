//package peer;
//
////import core.ProtocolMessage;
//
///**
// * ProtocolWorker
// */
//public class ProtocolWorker extends Worker {
//
//	/** core */
//	private ProtocolConnection core;
//
//	/**
//	 * Creates a new worker
//	 *
//	 * @param core core
//	 */
//	ProtocolWorker(ProtocolConnection core) {
//		this.core = core;
//	}
//
//	@Override
//	protected void work() {
//		ProtocolMessage request = core.listen();
//
//		if (request == null) {
//			// TODO: ignore?
//		}
//
//		PrtclMsgType type = request.getType();
//		//TODO: finish switch
//		switch (type) {
//		case PrtclMsgType.BACKUP:
//			break;
//		case PrtclMsgType.RESTORE:
//			break;
//		case PrtclMsgType.DELETE:
//			break;
//		case PrtclMsgType.CHUNKDELETED:
//			break;
//		default:
//			// Error
//			break;
//		}
//	}
//}