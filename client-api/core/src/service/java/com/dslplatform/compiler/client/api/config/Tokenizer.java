package com.dslplatform.compiler.client.api.config;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import javax.crypto.Cipher;

import org.apache.commons.codec.Charsets;
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

            final Certificate certificate = cf.generateCertificate(keyStream);
            final PublicKey publicKey = certificate.getPublicKey();

            cipher = Cipher.getInstance(algo);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        } catch (Exception e) {
            throw new Error(e);
        }
        return cipher;
    }

    private static String makeToken(
            final String username,
            final String password) {
        final String toToken = username + ":" + password + ":" + System.currentTimeMillis() / 1000;
        final byte[] message = toToken.getBytes(Charsets.UTF_8);
        try {
            return StringUtils.newStringUtf8(Base64.encodeBase64(cipher.doFinal(message), false, false));
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String tokenHeader(
            final String username,
            final String password) {
        return "Token " + Tokenizer.makeToken(username, password);
    }

    public static String basicHeader(
            final String username,
            final String password) {
        final byte[] bytes = (username + ":" + password).getBytes(Charsets.UTF_8);
        return "Authorization " + Base64.encodeBase64String(bytes);
    }
}
