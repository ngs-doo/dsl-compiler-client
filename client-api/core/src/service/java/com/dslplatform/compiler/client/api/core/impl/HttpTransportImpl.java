package com.dslplatform.compiler.client.api.core.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.slf4j.Logger;

import com.dslplatform.compiler.client.api.config.ClientConfiguration;
import com.dslplatform.compiler.client.api.config.KeystoreConfiguration;
import com.dslplatform.compiler.client.api.config.StreamLoader;
import com.dslplatform.compiler.client.api.core.HttpRequest;
import com.dslplatform.compiler.client.api.core.HttpResponse;
import com.dslplatform.compiler.client.api.core.HttpTransport;

public class HttpTransportImpl implements HttpTransport {
    private final Logger logger;
    private final ClientConfiguration clientConfiguration;
    private final StreamLoader streamLoader;
    private final SSLSocketFactory sslSocketFactory;

    public HttpTransportImpl(
            final Logger logger,
            final ClientConfiguration clientConfiguration,
            final StreamLoader streamLoader) throws IOException {
        this.logger = logger;
        this.clientConfiguration = clientConfiguration;
        this.streamLoader = streamLoader;
        sslSocketFactory = createSSLSocketFactory();
    }

    private SSLSocketFactory createSSLSocketFactory() throws IOException {
        try {
            final KeystoreConfiguration truststoreConfiguration = clientConfiguration.getTruststoreConfiguration();

            final TrustManager[] trustManagers;
            if (truststoreConfiguration != null) {
                final KeyStore truststore = KeyStore.getInstance(truststoreConfiguration.getType());
                final InputStream is = streamLoader.open(truststoreConfiguration.getPath());
                try {
                    truststore.load(is, truststoreConfiguration.getPassword());
                } finally {
                    is.close();
                }
                final TrustManagerFactory tmf = TrustManagerFactory.getInstance("PKIX");
                tmf.init(truststore);
                trustManagers = tmf.getTrustManagers();
                logger.debug("Initialized {} trust managers", trustManagers.length);
            } else {
                trustManagers = null;
                logger.trace("No trust managers were initialized");
            }

            final KeystoreConfiguration keystoreConfiguration = clientConfiguration.getKeystoreConfiguration();

            final KeyManager[] keyManagers;
            if (keystoreConfiguration != null) {
                final KeyStore keystore = KeyStore.getInstance(keystoreConfiguration.getType());
                final InputStream is = streamLoader.open(keystoreConfiguration.getPath());
                try {
                    keystore.load(is, keystoreConfiguration.getPassword());
                } finally {
                    is.close();
                }
                final KeyManagerFactory kmf = KeyManagerFactory.getInstance("PKIX");
                kmf.init(keystore, keystoreConfiguration.getPassword());
                keyManagers = kmf.getKeyManagers();
                logger.debug("Initialized {} key managers", keyManagers.length);
            } else {
                keyManagers = null;
                logger.trace("No key managers were initialized");
            }

            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagers, trustManagers, new SecureRandom());
            final SSLSocketFactory result = sslContext.getSocketFactory();
            logger.debug("Initialized SSl context");
            return result;
        } catch (final Exception e) {
            throw new IOException("Could not initialize SSL context", e);
        }
    }

    private static final Charset ENCODING = Charset.forName("UTF-8");

    public HttpResponse sendRequest(final HttpRequest request) throws IOException {
        final int timeoutMs = (int) clientConfiguration.getTimeout().getMillis();
        final URL targetUrl = new URL(clientConfiguration.getCompilerUri() + request.path + request.buildQuery());

        if (logger.isDebugEnabled()) {
            final StringBuilder sb = new StringBuilder();
            sb.append("Sending ").append(request.method).append(" request to ").append(targetUrl);
            for (final Map.Entry<String, List<String>> header : request.headers.entrySet()) {
                final String key = header.getKey();
                for (final String value : header.getValue()) {
                    sb.append("\n").append(key).append(": ").append(value);
                }
            }
            logger.debug(sb.toString());
        }

        if (logger.isTraceEnabled()) {
            if (request.body != null)
                try {
                    logger.trace("Request body: <{}>", new String(request.body, ENCODING));
                } catch (final Exception e) {
                    logger.trace("<could not decode request body via UTF-8>");
                }
        }

        final long startedAt = System.currentTimeMillis();
        final HttpURLConnection huc = (HttpURLConnection) targetUrl.openConnection();
        huc.setConnectTimeout(timeoutMs);
        huc.setReadTimeout(timeoutMs);

        if (huc instanceof HttpsURLConnection) {
            final HttpsURLConnection hsuc = (HttpsURLConnection) huc;
            hsuc.setSSLSocketFactory(sslSocketFactory);
        }

        huc.setRequestMethod(request.method.name());
        for (final Map.Entry<String, List<String>> header : request.headers.entrySet()) {
            for (final String value : header.getValue()) {
                huc.setRequestProperty(header.getKey(), value);
            }
        }

        if (request.body != null) {
            huc.setDoOutput(true);
            final OutputStream os = huc.getOutputStream();
            try {
                os.write(request.body);
            } finally {
                os.close();
            }
        }

        final int code = huc.getResponseCode();
        final boolean ok = code / 100 == 2;

        final InputStream is = ok
                ? huc.getInputStream()
                : huc.getErrorStream();
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

        final byte[] body = baos.toByteArray();

        final HttpResponse response = new HttpResponse(code, huc.getHeaderFields(), body);

        if (logger.isDebugEnabled()) {
            final long endedAt = System.currentTimeMillis();
            final int tookMs = (int) (endedAt - startedAt);
            final StringBuilder sb = new StringBuilder();
            sb.append("Received ").append(body.length).append(" bytes in ").append(tookMs).append(" ms (from ")
                    .append(targetUrl).append(")").append("\n");
            sb.append(response.headers.get(null).get(0));

            for (final Map.Entry<String, List<String>> header : response.headers.entrySet()) {
                final String key = header.getKey();
                for (final String value : header.getValue()) {
                    if (key == null) continue;
                    sb.append("\n").append(key).append(": ").append(value);
                }
            }
            logger.debug(sb.toString());
        }

        if (logger.isTraceEnabled()) {
            try {
                logger.trace("Response body: >{}<", new String(response.body, ENCODING));
            } catch (final Exception e) {
                logger.trace(">could not decode response body via UTF-8<");
            }
        }

        return response;
    }
}
