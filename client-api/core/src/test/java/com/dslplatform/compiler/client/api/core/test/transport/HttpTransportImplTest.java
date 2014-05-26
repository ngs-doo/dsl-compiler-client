package com.dslplatform.compiler.client.api.core.test.transport;

import com.dslplatform.compiler.client.api.config.*;
import com.dslplatform.compiler.client.api.core.HttpRequestBuilder;
import com.dslplatform.compiler.client.api.core.impl.HttpRequestBuilderImpl;
import com.dslplatform.compiler.client.api.core.impl.HttpTransportImpl;
import com.dslplatform.compiler.client.api.core.HttpTransport;
import com.dslplatform.compiler.client.api.core.mock.MockData;
import com.dslplatform.compiler.client.util.PathExpander;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class HttpTransportImplTest extends MockData {
    static Logger logger = LoggerFactory.getLogger(HttpTransportImplTest.class);
    static HttpRequestBuilder httpRequestBuilder;
    static HttpTransport httpTransport;

    //final String auth = Tokenizer.tokenHeader(validUser, validPassword);
    final String auth= Tokenizer.basicHeader(validUser, validPassword);
    @BeforeClass
    public static void createHttpRequestBuilder() {
        httpRequestBuilder = new HttpRequestBuilderImpl();
    }

    @BeforeClass
    public static void createHttpTransport() throws IOException {

        final Logger logger = org.slf4j.LoggerFactory.getLogger("core-test");
        final PathExpander pathExpander = new PathExpander(logger);
        final StreamLoader streamLoader = new StreamLoader(logger, pathExpander);
        final PropertyLoader propertyLoader = new PropertyLoader(logger, streamLoader);

        final ClientConfigurationFactory ccf = new PropertyClientConfigurationFactory(logger, propertyLoader, "/api.properties");
        final ClientConfiguration clientConfiguration = ccf.getClientConfiguration();

        httpTransport = new HttpTransportImpl(logger, clientConfiguration, streamLoader);

        //httpTransport = new HttpTransportMock();
    }
}
