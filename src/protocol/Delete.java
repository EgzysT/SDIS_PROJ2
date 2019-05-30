package protocol;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import chord.ChordNode;
import store.Store;
import utils.Logger;

/**
 * Delete protocol
 */
public abstract class Delete {

	static Map<String , Boolean> instances;

    static {
        instances = new ConcurrentHashMap<>();
	}
	
	private static boolean checkRequirements(String filePath, String fileID) {

		// Check if file is backed up
		if (fileID == null) {
			Logger.warning("Delete", "file " + filePath + " is not backed up");
			return false;
		}

		// Check if file is busy
		if (ProtocolHandler.isFileBusy(fileID)) {
            Logger.warning("Delete", "found another protocol instance for file " + fileID);
            return false;
		}

		return true;
	}

	public static void deleteFile(String filePath) {

		String fileID = Store.getFileID(filePath);

		if (!checkRequirements(filePath, fileID))
            return;

		if (instances.putIfAbsent(fileID, true) != null) {
            Logger.warning("Delete", "found another delete protocol instance for file " + fileID);
            return;
        }

		for (int chunkNo = 0; chunkNo < Store.files.get(fileID).chunks; chunkNo++) {
			ChordNode.instance().remove(fileID, chunkNo);
		}

		// TODO if a dead node comes back with store, send check messages

		Store.unregisterFile(fileID);

		instances.computeIfPresent(fileID, (k,v) -> null);

		Logger.info("Delete", "completed delete protocol for file " + fileID);
	}
}