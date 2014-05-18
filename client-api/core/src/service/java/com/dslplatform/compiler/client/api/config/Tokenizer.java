package com.dslplatform.compiler.client.api.config;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import javax.crypto.Cipher;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;

public class Tokenizer {
    private final static String algo = "RSA";
    private final static InputStream keyStream = Tokenizer.class.getResourceAsStream("/ngs-rsa.crt.der");

    private final static Cipher cipher = setCipher();

    private static Cipher setCipher() {
        final Cipher cipher;
        try {
            final CertificateFactory cf = CertificateFactory.getInstance("X509");

            final Certificate cert = cf.generateCertificate(keyStream);
            final PublicKey pkey = cert.getPublicKey();

            cipher = Cipher.getInstance(algo);
            cipher.init(Cipher.ENCRYPT_MODE, pkey);
        } catch (Exception e) {
            throw new Error(e);
        }
        return cipher;
    }

    private static String makeToken(
            final String username,
            final String password,
            final String projectid) {
        final String noproject = username + ":" + password + ":" + System.currentTimeMillis() / 1000;
        final String toToken = (projectid != null) ? noproject + ":" + projectid : noproject;
        final byte[] message = toToken.getBytes(Charset.forName("UTF-8"));
        try {
            return StringUtils.newStringUtf8(Base64.encodeBase64(cipher.doFinal(message), false, false));
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String tokenHeader(
            final String username,
            final String password,
            final String projectid) {
        return "Token " + Tokenizer.makeToken(username, password, projectid);
    }

    public static String userTokenHeader(
            final String username,
            final String password) {
        return tokenHeader(username, password, null);
    }
}
