package com.dslplatform.compiler.client.api.config;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import org.joda.time.Duration;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.slf4j.Logger;

class PropertyConfigurationParser implements ClientConfiguration {
    private final Logger logger;
    private final Properties properties;

    public PropertyConfigurationParser(final Logger logger, final Properties properties) {
        this.logger = logger;
        this.properties = properties;
    }

    private static final String COMPILER_URI_KEY = "compilerUri";
    private static final String TIMEOUT_KEY = "timeout";

    private static final String TRUSTSTORE_PATH_KEY = "truststorePath";
    private static final String TRUSTSTORE_TYPE_KEY = "truststoreType";
    private static final String TRUSTSTORE_PASSWORD_KEY = "truststorePassword";

    private static final String KEYSTORE_PATH_KEY = "keystorePath";
    private static final String KEYSTORE_TYPE_KEY = "keystoreType";
    private static final String KEYSTORE_PASSWORD_KEY = "keystorePassword";

    @Override
    public URI getCompilerUri() {
        logger.debug("About to read " + COMPILER_URI_KEY + " property ...");
        final String compilerUri = properties.getProperty(COMPILER_URI_KEY);
        if (compilerUri == null)
            throw new IllegalArgumentException("Could not read " + COMPILER_URI_KEY + " property");

        logger.trace("About to parse " + COMPILER_URI_KEY + " property: {}", compilerUri);
        try {
            final URI result = new URI(compilerUri);
            logger.debug("Parsed " + COMPILER_URI_KEY + " property: {}", compilerUri);
            return result;
        } catch (final URISyntaxException e) {
            throw new IllegalArgumentException("Could not parse " + COMPILER_URI_KEY + " property: " + compilerUri, e);
        }
    }

    @Override
    public Duration getTimeout() {
        logger.debug("About to read " + TIMEOUT_KEY + " property ...");
        final String timeout = properties.getProperty(TIMEOUT_KEY);
        if (timeout == null) throw new IllegalArgumentException("Could not read " + TIMEOUT_KEY + " property");

        logger.trace("About to parse " + TIMEOUT_KEY + " property: {}", timeout);
        try {
            final PeriodFormatter formatter =
                    new PeriodFormatterBuilder().appendMinutes().appendSuffix("m").appendSeparator(" ").appendSeconds()
                            .appendSuffix("s").toFormatter();

            final Duration result = formatter.parsePeriod(timeout).toStandardDuration();
            logger.debug("Parsed " + TIMEOUT_KEY + " property: {}", result);
            return result;
        } catch (final IllegalArgumentException e) {
            throw new IllegalArgumentException("Could not parse " + TIMEOUT_KEY + " property: " + timeout, e);
        }
    }

    @Override
    public KeystoreConfiguration getTruststoreConfiguration() {
        return new KeystoreConfiguration() {
            @Override
            public String getPath() {
                logger.debug("About to read " + TRUSTSTORE_PATH_KEY + " property ...");
                final String truststorePath = properties.getProperty(TRUSTSTORE_PATH_KEY);
                if (truststorePath == null) {
                    logger.debug("Could not read " + TRUSTSTORE_PATH_KEY + " property, returning null");
                    return null;
                }

                logger.debug("Parsed " + TRUSTSTORE_PATH_KEY + " property: {}", truststorePath);
                return truststorePath;
            }

            @Override
            public String getType() {
                logger.debug("About to read " + TRUSTSTORE_TYPE_KEY + " property ...");
                final String truststoreType = properties.getProperty(TRUSTSTORE_TYPE_KEY);

                if (truststoreType != null) {
                    logger.debug("Parsed " + TRUSTSTORE_TYPE_KEY + " property: {}", truststoreType);
                    return truststoreType;
                }

                logger.debug("Could not read " + TRUSTSTORE_TYPE_KEY + " property, using extension from the "
                        + TRUSTSTORE_PATH_KEY + " property ...");
                final String truststorePath = getPath();
                if (truststorePath == null) return null;

                final String truststoreTypeFromPath = truststorePath.replaceFirst(".*\\.", "");
                logger.debug("Derived " + TRUSTSTORE_TYPE_KEY + " property from the path: {}", truststoreTypeFromPath);
                return truststoreTypeFromPath;
            }

            @Override
            public char[] getPassword() {
                logger.debug("About to read " + TRUSTSTORE_PASSWORD_KEY + " property ...");
                final String truststorePassword = properties.getProperty(TRUSTSTORE_PASSWORD_KEY);
                if (truststorePassword == null) {
                    logger.debug("Could not read " + TRUSTSTORE_PASSWORD_KEY + " property, returning null");
                    return null;
                }

                logger.debug("Parsed " + TRUSTSTORE_PASSWORD_KEY + " property: {}", "****");
                return truststorePassword.toCharArray();
            }
        };
    }

    @Override
    public KeystoreConfiguration getKeystoreConfiguration() {
        return new KeystoreConfiguration() {
            @Override
            public String getPath() {
                logger.debug("About to read " + KEYSTORE_PATH_KEY + " property ...");
                final String keystorePath = properties.getProperty(KEYSTORE_PATH_KEY);
                if (keystorePath == null) {
                    logger.debug("Could not read " + KEYSTORE_PATH_KEY + " property, returning null");
                    return null;
                }

                logger.debug("Parsed " + KEYSTORE_PATH_KEY + " property: {}", keystorePath);
                return keystorePath;
            }

            @Override
            public String getType() {
                logger.debug("About to read " + KEYSTORE_TYPE_KEY + " property ...");
                final String keystoreType = properties.getProperty(KEYSTORE_TYPE_KEY);

                if (keystoreType != null) {
                    logger.debug("Parsed " + KEYSTORE_TYPE_KEY + " property: {}", keystoreType);
                    return keystoreType;
                }

                logger.debug("Could not read " + KEYSTORE_TYPE_KEY + " property, using extension from the "
                        + KEYSTORE_PATH_KEY + " property ...");
                final String keystorePath = getPath();
                if (keystorePath == null) return null;

                final String keystoreTypeFromPath = keystorePath.replaceFirst(".*\\.", "");
                logger.debug("Derived " + KEYSTORE_TYPE_KEY + " property from the path: {}", keystoreTypeFromPath);
                return keystoreTypeFromPath;
            }

            @Override
            public char[] getPassword() {
                logger.debug("About to read " + KEYSTORE_PASSWORD_KEY + " property ...");
                final String keystorePassword = properties.getProperty(KEYSTORE_PASSWORD_KEY);
                if (keystorePassword == null) {
                    logger.debug("Could not read " + KEYSTORE_PASSWORD_KEY + " property, returning null");
                    return null;
                }

                logger.debug("Parsed " + KEYSTORE_PASSWORD_KEY + " property: {}", "****");
                return keystorePassword.toCharArray();
            }
        };
    }
}
