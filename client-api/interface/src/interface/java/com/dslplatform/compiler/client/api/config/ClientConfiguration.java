package com.dslplatform.compiler.client.api.config;

import java.net.URI;

import org.joda.time.Duration;

public interface ClientConfiguration {
    /** Endpoint compiler URL */
    public URI getCompilerUri();

    /** HTTP request timeout duration */
    public Duration getTimeout();

    /** null implies no truststore */
    public KeystoreConfiguration getTruststoreConfiguration();

    /** null implies no keystore */
    public KeystoreConfiguration getKeystoreConfiguration();
}
