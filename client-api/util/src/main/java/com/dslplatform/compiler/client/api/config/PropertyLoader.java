package com.dslplatform.compiler.client.api.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;

public class PropertyLoader {
    private final Logger logger;
    private final StreamLoader streamLoader;

    public PropertyLoader(final Logger logger, final StreamLoader streamLoader) {
        this.logger = logger;
        this.streamLoader = streamLoader;
    }

    public Properties read(final String path) throws IOException {
        final InputStream is = streamLoader.open(path);
        try {
            final Properties properties = new Properties();
            properties.load(is);

            logger.debug("Successfully loaded {} properties", properties.size());
            return properties;
        } finally {
            is.close();
        }
    }
}
