package store;

import java.io.Serializable;

/**
 * File info
 */
public class FileInfo implements Serializable {

    /** Path */
    public String  filePath;

    /** Number of chunks */
    public Integer chunks;

    public FileInfo(String path, Integer nr) {
        filePath = path;
        chunks = nr;
    }
}
