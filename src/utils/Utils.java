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

    public static BigInteger start(BigInteger n, Integer i) {
        return n.add(new BigInteger("2").pow(i - 1));
    }

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

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}
