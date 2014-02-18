package com.dslplatform.compiler.client.api.config;

import java.io.IOException;

import org.slf4j.Logger;

public class PropertyClientConfigurationFactory implements ClientConfigurationFactory {
    private final Logger logger;
    private final PropertyLoader propertyLoader;
    private final String configurationPath;

    public PropertyClientConfigurationFactory(
            final Logger logger,
            final PropertyLoader propertyLoader,
            final String configurationPath) {
        this.logger = logger;
        this.propertyLoader = propertyLoader;
        this.configurationPath = configurationPath;
    }

    @Override
    public ClientConfiguration getClientConfiguration() throws IOException {
        return new CachingConfigurationProxy(new PropertyConfigurationParser(logger,
                propertyLoader.read(configurationPath)));
    }
}
