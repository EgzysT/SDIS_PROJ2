package protocol;

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
		if (!Store.isBackedUp(fileID)) {
			Logger.warning("Delete", "file " + filePath + " was not previously backed up");
			return false;
		}

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
            Logger.warning("Deleete", "found another delete protocol instance for file " + fileID);
            return;
        }
	}
}