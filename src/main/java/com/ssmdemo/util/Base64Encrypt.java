package com.ssmdemo.util;

import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

/**
 * base64加密类
 */
public class Base64Encrypt {

    private static final Encoder encoder = Base64.getEncoder();
    private static final Decoder decoder = Base64.getDecoder();

    public static String encryptUserName(String userName) {
        byte[] b = userName.getBytes();
        for (int i = 0; i < b.length; i++) {
            if (b[i] % 2 == 0) {
                b[i] = (byte) (b[i] + 1);
            } else {
                b[i] = (byte) (b[i] - 1);
            }
        }
        return new String(encoder.encode(b));
    }

    public static String decryptUserName(String userName) {
        byte[] b = userName.getBytes();
        b = decoder.decode(b);
        for (int i = 0; i < b.length; i++) {
            if (b[i] % 2 == 0) {
                b[i] = (byte) (b[i] + 1);
            } else {
                b[i] = (byte) (b[i] - 1);
            }
        }
        return new String(b);
    }

    public static String byteArrayToString(byte[] b) {
        return encoder.encodeToString(b);
    }
}
