package com.dslplatform.compiler.client.cmdline;

import com.dslplatform.compiler.client.Api;
import com.dslplatform.compiler.client.ApiImpl;
import com.dslplatform.compiler.client.api.core.impl.HttpRequestBuilderImpl;
import com.dslplatform.compiler.client.api.core.impl.UnmanagedDSLImpl;
import com.dslplatform.compiler.client.api.core.mock.HttpTransportMock;
import com.dslplatform.compiler.client.api.core.mock.UnmanagedDSLMock;
import com.dslplatform.compiler.client.cmdline.parser.Arguments;
import com.dslplatform.compiler.client.cmdline.tools.IOMock;
import com.dslplatform.compiler.client.cmdline.tools.MockCommandLinePrompt;
import com.dslplatform.compiler.client.cmdline.tools.TestArguments;
import com.dslplatform.compiler.client.cmdline.tools.TestingOutput;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class DeployUnmanagedServerTest {

    @Test
    public void testDeployUnmanagedServerTest() throws IOException { /* todo - will need temp folder to unit test this one fully */
        Logger logger = LoggerFactory.getLogger("dcc-test");
        Arguments arguments = new TestArguments("/deploy_unmanaged_server.props", logger);
        CommandLinePrompt clcp = new MockCommandLinePrompt(true, true, true, true, true, true, true, true, true, true);
        IOMock io = new IOMock();
        Api api = new ApiImpl(new HttpRequestBuilderImpl(), new HttpTransportMock(), UnmanagedDSLMock.mock_single_integrated);
        TestingOutput output = new TestingOutput();
        CLCAction action = new ActionDefinition(api, logger, output, arguments, clcp, io);

        action.deployUnmanagedServer();
    }
}
