package com.dslplatform.compiler.client.api.config;

import java.net.URI;

import org.joda.time.Duration;

class CachingConfigurationProxy implements ClientConfiguration {
    private final ClientConfiguration underlying;

    public CachingConfigurationProxy(final ClientConfiguration underlying) {
        this.underlying = underlying;
    }

    private URI compilerUri;

    @Override
    public URI getCompilerUri() {
        return compilerUri == null
                ? compilerUri = underlying.getCompilerUri()
                : compilerUri;
    }

    private Duration timeout;

    @Override
    public Duration getTimeout() {
        return timeout == null
                ? timeout = underlying.getTimeout()
                : timeout;
    }

    private boolean truststoreConfigurationNotFound;
    private KeystoreConfiguration truststoreConfiguration;

    @Override
    public KeystoreConfiguration getTruststoreConfiguration() {
        if (truststoreConfiguration != null || truststoreConfigurationNotFound) return truststoreConfiguration;

        final KeystoreConfiguration truststoreConfiguration = underlying.getTruststoreConfiguration();
        final String truststorePath = truststoreConfiguration.getPath();
        if (truststorePath != null) {
            final String truststoreType = truststoreConfiguration.getType();
            if (truststoreType != null) {
                final char[] truststorePassword = truststoreConfiguration.getPassword();
                if (truststorePassword != null)
                    return this.truststoreConfiguration =
                            new SimpleKeystoreConfiguration(truststorePath, truststoreType, truststorePassword);
            }
        }

        truststoreConfigurationNotFound = true;
        return null;
    }

    private boolean keystoreConfigurationNotFound;
    private KeystoreConfiguration keystoreConfiguration;

    @Override
    public KeystoreConfiguration getKeystoreConfiguration() {
        if (keystoreConfiguration != null || keystoreConfigurationNotFound) return null;

        final KeystoreConfiguration keystoreConfiguration = underlying.getKeystoreConfiguration();
        final String keystorePath = keystoreConfiguration.getPath();
        if (keystorePath != null) {
            final String keystoreType = keystoreConfiguration.getType();
            if (keystoreType != null) {
                final char[] keystorePassword = keystoreConfiguration.getPassword();
                if (keystorePassword != null)
                    return this.keystoreConfiguration =
                            new SimpleKeystoreConfiguration(keystorePath, keystoreType, keystorePassword);
            }
        }

        keystoreConfigurationNotFound = true;
        return null;
    }
}
