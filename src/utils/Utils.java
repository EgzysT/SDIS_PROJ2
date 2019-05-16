package utils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class Utils {

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

    public static Integer start(Integer n, Integer i) {
        return n + (int) Math.pow(2, i - 1);
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
}
