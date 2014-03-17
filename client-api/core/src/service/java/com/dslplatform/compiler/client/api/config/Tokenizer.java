package com.dslplatform.compiler.client.api.config;

import javax.crypto.Cipher;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

public class Tokenizer {

    private final static String algo = "RSA";
    private final static InputStream keyStream = Tokenizer.class.getResourceAsStream("/ngs-rsa.crt.der");

    private final static Cipher cipher = setCipher();
    private static Cipher  setCipher() {
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

    public static String makeToken(
            final String username,
            final String password) {
        return makeToken(username, password, null);
    }

    public static String makeToken(
            final String username,
            final String password,
            final String projectid)  {
        final Charset charset = Charset.forName("UTF-8");

        final String noproject = username + ":" + password + ":" + System.currentTimeMillis() / 1000;
        final String toToken = (projectid != null) ? noproject + ":" + projectid : noproject;
        final byte[] message = toToken.getBytes(charset);

        try {
            final String ct64 = org.apache.commons.codec.binary.Base64.encodeBase64String(cipher.doFinal(message));
            return ct64;
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
