package com.dslplatform.compiler.client;

import com.dslplatform.compiler.client.api.config.*;
import com.dslplatform.compiler.client.api.core.HttpTransport;
import com.dslplatform.compiler.client.api.core.impl.HttpTransportImpl;
import com.dslplatform.compiler.client.io.PathExpander;
import org.slf4j.Logger;

import java.io.IOException;

public class HttpTransportProvider {
    public static HttpTransport httpTransport() throws IOException {

        final Logger logger = org.slf4j.LoggerFactory.getLogger("httptransport");
        final PathExpander pathExpander = new PathExpander(logger);
        final StreamLoader streamLoader = new StreamLoader(logger, pathExpander);
        final PropertyLoader propertyLoader = new PropertyLoader(logger, streamLoader);

        final ClientConfigurationFactory
                ccf = new PropertyClientConfigurationFactory(logger, propertyLoader, "/api.properties");
        final ClientConfiguration clientConfiguration = ccf.getClientConfiguration();

        return new HttpTransportImpl(logger, clientConfiguration, streamLoader);
    }
}
