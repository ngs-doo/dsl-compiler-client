package com.dslplatform.compiler.client.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.security.SecureRandom;
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
    private final Logger logger;
    private final ApiProperties apiProperties;

    public ApiCall(
            final Logger logger,
            final ApiProperties apiProperties) {
        this.logger = logger;
        this.apiProperties = apiProperties;
    }

    private SSLSocketFactory sslSocketFactory;

    private static SSLSocketFactory createSSLSocketFactory(
            final String truststoreName,
            final char[] truststorePassword) throws Exception {
        final KeyStore truststore = KeyStore.getInstance("jks");
        truststore.load(ApiCall.class.getResourceAsStream(truststoreName),
                truststorePassword);

        final TrustManagerFactory tMF = TrustManagerFactory.getInstance("PKIX");
        tMF.init(truststore);

        final SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tMF.getTrustManagers(), new SecureRandom());
        return sslContext.getSocketFactory();

    }

    Response read(
            final Target target,
            final UUID requestID,
            final byte[] body,
            final int timeout) throws IOException {

        final URL targetUrl = new URL(apiProperties.getApiUrl() + target.branch
                + '/' + requestID);

        logger.debug("Sending request to " + targetUrl);

        final HttpURLConnection huc = (HttpURLConnection) targetUrl
                .openConnection();

        huc.setConnectTimeout(timeout);
        huc.setReadTimeout(timeout);

        if (huc instanceof HttpsURLConnection) {
            try {
                final HttpsURLConnection hsUC = (HttpsURLConnection) huc;
                if (sslSocketFactory == null) {
                    logger.trace("Initializing SSL connection...");
                    sslSocketFactory = createSSLSocketFactory(
                            apiProperties.getTruststorePath(),
                            apiProperties.getTruststorePassword());
                }
                hsUC.setSSLSocketFactory(sslSocketFactory);
            } catch (final Exception e) {
                logger.error("Could not initialize SSL socket factory:" + e);
            }
        }

        if (body != null) {
            huc.setDoOutput(true);
            huc.setRequestMethod("POST");

            final OutputStream os = huc.getOutputStream();
            os.write(body);
            os.close();
        }

        final int code = huc.getResponseCode();
        final boolean ok = code / 100 == 2;

        final InputStream is = ok ? huc.getInputStream() : huc.getErrorStream();
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final byte[] buffer = new byte[8192];

        try {
            while (true) {
                final int read = is.read(buffer);
                if (read == -1) {
                    break;
                }
                baos.write(buffer, 0, read);
            }
        } finally {
            is.close();
        }

        final byte[] response = baos.toByteArray();
        logger.trace("Recevied " + response.length + " bytes");

        return new Response(ok, code, response);
    }

    public RunningTask call(final Param... params) throws IOException {
        final ParamMap paramMap = new ParamMap();
        paramMap.add(params);

        if (paramMap.firstOf(Target.class) == null) {
            paramMap.add(new Target(apiProperties.getBranch(), apiProperties
                    .getVersion()));
        }
        if (paramMap.firstOf(Environment.class) == null) {
            paramMap.add(new Environment());
        }

        final String content = paramMap.toXML();
        logger.trace("Request payload: " + content);

        final byte[] body = JavaSerialization.serialize(content);
        final int timeout = apiProperties.getTimeout();
        final int pollInterval = apiProperties.getPollInterval();

        final Target target = paramMap.firstOf(Target.class);
        return new RunningTask(logger, this, target, body, pollInterval,
                timeout);
    }
}
