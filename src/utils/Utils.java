package utils;

import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public abstract class Utils {

    /**
     * Generates node's identifier
     * @param address Node's address
     * @return Node's identifier
     */
    public static String generateChordID(String address) {

        StringBuilder hexString = new StringBuilder();

        try {
            byte[] hash = MessageDigest.getInstance("SHA-1").digest(address.getBytes());

            for (byte b : hash) {
                hexString.append(Integer.toHexString(0xFF & b));
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        return hexString.toString();
    }

    /**
     * Generates file's identifier
     * @param filePath File path
     * @return File's identifier
     */
    public static String generateFileID(String filePath) {

        StringBuilder hexString = new StringBuilder();

        try {
            Path p = Paths.get(filePath);
            String metadata = p.getFileName().toString();

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(metadata.getBytes());

            for (byte b : hash) {
                hexString.append(Integer.toHexString((b & 0xF0) >> 4));
                hexString.append(Integer.toHexString(b & 0x0F));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return hexString.toString();
    }

    /**
     * Checks if value between lower and upper bounds
     * @param value Value to check
     * @param lower Lower bound
     * @param upper Upper bound
     * @param closed Include upper bound
     * @return True if valus is between lower and upper bound, false otherwise
     */
    public static Boolean inRange(BigInteger value, BigInteger lower, BigInteger upper, Boolean closed) {

        if (lower.compareTo(upper) < 0) {
            if (closed)
                return value.compareTo(lower) > 0 && value.compareTo(upper) <= 0;
            else
                return value.compareTo(lower) > 0 && value.compareTo(upper) < 0;
        } else {
            if (closed)
                return value.compareTo(lower) > 0 || value.compareTo(upper) <= 0;
            else
                return value.compareTo(lower) > 0 || value.compareTo(upper) < 0;
        }
    }
}
