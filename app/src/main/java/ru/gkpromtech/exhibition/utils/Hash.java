package ru.gkpromtech.exhibition.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hash {
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String hash(String str) throws NoSuchAlgorithmException {
        return bytesToHex(MessageDigest.getInstance("MD5").digest(str.getBytes()));
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; ++j) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
