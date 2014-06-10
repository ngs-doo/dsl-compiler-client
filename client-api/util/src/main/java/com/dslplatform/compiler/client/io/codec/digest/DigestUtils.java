package com.dslplatform.compiler.client.io.codec.digest;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DigestUtils {
    public static byte[] sha1(final byte[] data) {
        try {
            return MessageDigest.getInstance("SHA-1").digest(data);
        } catch (final NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-1 not available!", e);
        }
    }
}
