package com.dslplatform.compiler.client.api;

import java.util.Properties;

import com.dslplatform.compiler.client.io.Logger;

public class ApiProperties {
    private final Logger logger;
    private final Properties properties;

    public ApiProperties(
            final Logger logger,
            final Properties properties) {
        this.logger = logger;
        this.properties = properties;
    }

    private String apiUrl;
    private String branch;
    private String version;

    private Integer pollInterval;
    private Integer timeout;

    private String truststorePath;
    private char[] truststorePassword;

    public String getApiUrl() {
        if (apiUrl == null) {
            apiUrl = readString("api-url");
        }
        return apiUrl;
    }

    public String getBranch() {
        if (branch == null) {
            branch = readString("branch");
        }
        return branch;
    }

    public String getVersion() {
        if (version == null) {
            version = readString("version");
        }
        return version;
    }

    public int getPollInterval() {
        if (pollInterval == null) {
            pollInterval = readInteger("poll-interval");
        }
        return pollInterval;
    }

    public int getTimeout() {
        if (timeout == null) {
            timeout = readInteger("timeout");
        }
        return timeout;
    }

    public String getTruststorePath() {
        if (truststorePath == null) {
            truststorePath = readString("truststore-path");
        }
        return truststorePath;
    }

    public char[] getTruststorePassword() {
        if (truststorePassword == null) {
            truststorePassword = readString("truststore-password")
                    .toCharArray();
        }
        return truststorePassword;
    }

    private int readInteger(final String key) {
        return Integer.parseInt(readString(key));
    }

    private String readString(final String key) {
        logger.trace("About to read property: " + key);
        final String value = properties.getProperty(key);
        if (value == null) {
            throw new IllegalArgumentException("Could not read property: "
                    + key);
        }
        logger.debug("Read property: " + value);
        return value;
    }
}
