package com.dslplatform.compiler.client.api.config;

import java.io.IOException;

public interface ClientConfigurationFactory {
    public ClientConfiguration getClientConfiguration() throws IOException;
}
