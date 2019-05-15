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

}
