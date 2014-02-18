package com.dslplatform.compiler.client.api.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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

import com.dslplatform.client.HttpAuthorization;
import com.dslplatform.compiler.client.api.config.ClientConfiguration;
import com.dslplatform.compiler.client.api.config.KeystoreConfiguration;
import com.dslplatform.compiler.client.api.config.StreamLoader;
import com.dslplatform.compiler.client.api.model.http.Header;
import com.dslplatform.compiler.client.api.model.http.Method;
import com.dslplatform.compiler.client.api.model.http.Request;
import com.dslplatform.compiler.client.api.model.http.Response;

public class HttpRequest {
    private final Logger logger;
    private final ClientConfiguration clientConfiguration;
    private final StreamLoader streamLoader;
    private final SSLSocketFactory sslSocketFactory;
    private final HttpAuthorization httpAuthorization;

    public HttpRequest(
            final Logger logger,
            final ClientConfiguration clientConfiguration,
            final StreamLoader streamLoader,
            final HttpAuthorization httpAuthorization) throws IOException {
        this.logger = logger;
        this.clientConfiguration = clientConfiguration;
        this.streamLoader = streamLoader;
        sslSocketFactory = createSSLSocketFactory();
        this.httpAuthorization = httpAuthorization;
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

    private static final String ALPHA_PATH = "Alpha.svc/";
    private static final String MIME_TYPE = "application/json";

    public Request builder(final Method method, final String path) {
        try {
            final Request request =
                    new Request().setMethod(method).setUrl(
                            new URI(clientConfiguration.getCompilerUri() + ALPHA_PATH + path));

            final List<Header> headers = request.getHeaders();
            headers.add(new Header("Accept", MIME_TYPE));

            if (method != Method.GET) {
                headers.add(new Header("Content-Type", MIME_TYPE));
            }

            for (final String authorizationHeader : httpAuthorization.getAuthorizationHeaders()) {
                headers.add(new Header("Authorization", authorizationHeader));
            }

            return request;
        } catch (final URISyntaxException e) {
            throw new RuntimeException("Could not build request URL", e);
        }
    }

    public Response sendRequest(final Request request) throws IOException {
        final int timeoutMs = (int) clientConfiguration.getTimeout().getMillis();
        final URL targetUrl = request.getUrl().toURL();

        if (logger.isDebugEnabled()) {
            logger.debug("Sending request to " + targetUrl);

            if (logger.isTraceEnabled()) {
                logger.trace("    {} {}", request.getMethod(), request.getUrl());
                for (final Header header : request.getHeaders()) {
                    logger.trace("    {}:{}", header.getKey(), header.getValue());
                }
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

        huc.setRequestMethod(request.getMethod().name());
        for (final Header header : request.getHeaders()) {
            huc.setRequestProperty(header.getKey(), header.getValue());
        }

        if (request.getBody() != null) {
            huc.setDoOutput(true);
            final OutputStream os = huc.getOutputStream();
            try {
                os.write(request.getBody());
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
        final long endedAt = System.currentTimeMillis();

        final Response response = new Response().setCode(code).setBody(body);

        for (final Map.Entry<String, List<String>> header : huc.getHeaderFields().entrySet()) {
            final String key = header.getKey();
            for (final String value : header.getValue()) {
                if (key == null) {
                    response.setStatus(value);
                } else {
                    response.getHeaders().add(new Header(header.getKey(), value));
                }
            }
        }

        if (logger.isDebugEnabled()) {
            final int tookMs = (int) (endedAt - startedAt);
            logger.debug("Recevied {} bytes in {} ms", body.length, tookMs);

            if (logger.isTraceEnabled()) {
                logger.trace("    {}", response.getStatus());
                for (final Header header : response.getHeaders()) {
                    logger.trace("    {}:{}", header.getKey(), header.getValue());
                }
            }
        }

        return response;
    }
}
