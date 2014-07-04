package com.dslplatform.compiler.client;

import com.dslplatform.compiler.client.api.config.*;
import com.dslplatform.compiler.client.api.core.HttpTransport;
import com.dslplatform.compiler.client.api.core.impl.HttpTransportImpl;
import org.slf4j.Logger;

import java.io.IOException;

public class HttpTransportProvider {
    public static HttpTransport httpTransport() throws IOException {

        final Logger logger = org.slf4j.LoggerFactory.getLogger("httptransport");
        final StreamLoader streamLoader = new StreamLoader(logger);
        final PropertyLoader propertyLoader = new PropertyLoader(logger);

        final ClientConfigurationFactory
                ccf = new PropertyClientConfigurationFactory(logger, propertyLoader, "/api.properties");
        final ClientConfiguration clientConfiguration = ccf.getClientConfiguration();

        return new HttpTransportImpl(logger, clientConfiguration, streamLoader);
    }
}
