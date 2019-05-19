package store;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Store {

    public static Map<String, String> files;

    static {
        files = new ConcurrentHashMap<>();
    }

    private Store() {}

    public static boolean isBackedUp(String fileID) {
        return files.containsKey(fileID);
    }

    public static void backupFile() {


    }





}
