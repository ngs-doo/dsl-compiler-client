package com.dslplatform.compiler.client.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Properties;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import com.dslplatform.compiler.client.api.params.Environment;
import com.dslplatform.compiler.client.api.params.Param;
import com.dslplatform.compiler.client.api.params.Target;
import com.dslplatform.compiler.client.io.Logger;

public class ApiCall {
    private static final String API_URL;
    private static final String VERSION;
    private static final int POLL_INTERVAL;
    private static final int TIMEOUT;

    private static final SSLSocketFactory socketFactory;

    static {
        try {
            final Properties apiProperties = new Properties();
            apiProperties.load(ApiCall.class
                    .getResourceAsStream("api.properties"));

            API_URL = apiProperties.getProperty("api-url");
            VERSION = apiProperties.getProperty("version");
            POLL_INTERVAL = Integer.parseInt(apiProperties
                    .getProperty("poll-interval"));
            TIMEOUT = Integer.parseInt(apiProperties.getProperty("timeout"));

            final KeyStore truststore = KeyStore.getInstance("jks");
            truststore.load(ApiCall.class.getResourceAsStream(apiProperties
                    .getProperty("truststore-name")), apiProperties
                    .getProperty("truststore-password").toCharArray());

            final TrustManagerFactory tMF = TrustManagerFactory
                    .getInstance("PKIX");
            tMF.init(truststore);

            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tMF.getTrustManagers(), new SecureRandom());

            socketFactory = sslContext.getSocketFactory();
        } catch (final Exception e) {
            throw new RuntimeException("Could not initialize API connection", e);
        }
    }

    private static Response read(
            final Target target,
            final byte[] body,
            final String params) throws IOException {

        final URL targetUrl = new URL(API_URL + target.branch + '/'
                + target.version + params);

        final HttpURLConnection hUC = (HttpURLConnection) targetUrl
                .openConnection();
        if (hUC instanceof HttpsURLConnection) {
            final HttpsURLConnection hsUC = (HttpsURLConnection) hUC;
            hsUC.setSSLSocketFactory(socketFactory);
        }

        hUC.setDoOutput(true);
        hUC.setRequestMethod("PUT");

        final OutputStream oS = hUC.getOutputStream();
        oS.write(body);
        oS.close();

        final int code = hUC.getResponseCode();
        final boolean ok = code / 100 == 2;

        final InputStream iS = ok ? hUC.getInputStream() : hUC.getErrorStream();
        final ByteArrayOutputStream bAOS = new ByteArrayOutputStream();
        final byte[] buffer = new byte[8192];

        try {
            while (true) {
                final int read = iS.read(buffer);
                if (read == -1) break;
                bAOS.write(buffer, 0, read);
            }
        } finally {
            iS.close();
        }

        return new Response(ok, code, bAOS.toByteArray());
    }

    private final Logger logger;

    public ApiCall(
            final Logger logger) {
        this.logger = logger;
    }

    public RunningTask call(final Param... params) throws IOException {
        final ParamMap paramMap = new ParamMap();
        paramMap.add(params);

        if (paramMap.firstOf(Target.class) == null) {
            paramMap.add(new Target(VERSION));
        }
        if (paramMap.firstOf(Environment.class) == null) {
            paramMap.add(new Environment());
        }

        final byte[] body = paramMap.toXML().getBytes(Charset.forName("UTF-8"));

        final Target target = paramMap.firstOf(Target.class);
        final Response response = read(target, body, "");

        return new RunningTask(target, response, POLL_INTERVAL, TIMEOUT);
    }

    static Response await(
            final Target target,
            final UUID requestID,
            final int pollInterval,
            final int afterOrdinal,
            final byte[] body) throws IOException {

        return read(target, body, "/response?id=" + requestID + "&timeout="
                + pollInterval + "&ordinal=" + afterOrdinal);
    }
}
