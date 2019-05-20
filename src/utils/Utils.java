package utils;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class Utils {

    private Utils() {}

    public static BigInteger hash(String toHash) {

        BigInteger result = null;

        try {
            byte[] hash = MessageDigest.getInstance("SHA-1").digest(toHash.getBytes());

            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                hexString.append(Integer.toHexString(0xFF & b));
            }

            result = new BigInteger(hexString.toString(), 16);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        return result;
    }

    public static String generateFileID(String filePath) {

        StringBuilder hexString = new StringBuilder();

        try {
            Path p = Paths.get(filePath);
            BasicFileAttributes view = Files.readAttributes(p, BasicFileAttributes.class);
            String metadata = p.getFileName().toString() + view.lastModifiedTime().toString();

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

    public static boolean in_range(Integer value, Integer lower, Integer upper, Boolean closed) {

        if (lower < upper) {
            if (closed)
                return value > lower && value <= upper;
            else
                return value > lower && value < upper;
        } else {
            if (closed)
                return value > lower || value <= upper;
            else
                return value > lower || value < upper;
        }
    }

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}
