package protocol;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import chord.ChordNode;
import store.Store;
import utils.Logger;

/**
 * Delete
 */
public class Delete {

	static Map<String , Boolean> instances;

    static {
        instances = new ConcurrentHashMap<>();
	}
	
	private static boolean checkRequirements(String filePath, String fileID) {
//		if (!Store.isBackedUp(fileID)) {
//			Logger.warning("Delete", "file " + filePath + " was not previously backed up");
//			return false;
//		}

		if (ProtocolHandler.isFileBusy(fileID)) {
            Logger.warning("Backup", "found another protocol instance for file " + fileID);
            return false;
		}
		return true;
	}

	public static void deleteFile(String filePath) {
		String fileID = Store.getFile(filePath);

		if (!checkRequirements(filePath, fileID))
            return;

		if (instances.putIfAbsent(fileID, true) != null) {
            Logger.warning("Delete", "found another delete protocol instance for file " + fileID);
            return;
        }

		for (int chunkNo = 0; chunkNo < Store.files.get(fileID).chunks; chunkNo++) {
			ChordNode.instance().remove(fileID, chunkNo);
		}

		instances.computeIfPresent(fileID, (k,v) -> null);

		// TODO
		Store.files.remove(fileID);

		Logger.info("Delete", "completed delete protocol for file " + fileID);
	}
}