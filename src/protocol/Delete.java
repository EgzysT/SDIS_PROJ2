package protocol;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import chord.ChordNode;
import store.Store;
import utils.Logger;
import utils.Utils;

/**
 * Delete protocol
 */
public abstract class Delete {

	static Map<String , Boolean> instances;

    static {
        instances = new ConcurrentHashMap<>();
	}

	/**
	 * Checks requirements before delete
	 * @param fileID File identifier
	 * @return True if all requirements are met, false otherwise
	 */
	private static boolean checkRequirements(String fileID) {

		if (!ProtocolHandler.isFileBackedUp(fileID)) {
			Logger.warning("Delete", "file " + fileID + " is not backed up");
			return false;
		}

		// Check if file is busy
		if (ProtocolHandler.isFileBusy(fileID)) {
            Logger.warning("Delete", "found another protocol instance for file " + fileID);
            return false;
		}

		return true;
	}

	/**
	 * Delete file
	 * @param filePath File path
	 */
	public static void deleteFile(String filePath) {

		String fileID = Utils.generateFileID(filePath);

		if (!checkRequirements(fileID))
            return;

		if (instances.putIfAbsent(fileID, true) != null) {
            Logger.warning("Delete", "found another delete protocol instance for file " + fileID);
            return;
        }

		boolean stop = false;
		int chunkNo = 0;

		do {
			if (ChordNode.instance().remove(fileID, chunkNo))
				Logger.fine("Delete", "deleted chunk #" + chunkNo + " from file " + fileID);
			else
				stop = true;

			chunkNo++;

		} while (!stop);

		instances.computeIfPresent(fileID, (k,v) -> null);

		Logger.info("Delete", "completed delete protocol for file " + fileID);
	}
}