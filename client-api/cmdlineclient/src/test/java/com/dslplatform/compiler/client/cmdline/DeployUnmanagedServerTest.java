package com.dslplatform.compiler.client.cmdline;

import com.dslplatform.compiler.client.Api;
import com.dslplatform.compiler.client.ApiImpl;
import com.dslplatform.compiler.client.api.core.impl.HttpRequestBuilderImpl;
import com.dslplatform.compiler.client.api.core.mock.HttpTransportMock;
import com.dslplatform.compiler.client.api.core.mock.UnmanagedDSLMock;
import com.dslplatform.compiler.client.cmdline.parser.Arguments;
import com.dslplatform.compiler.client.cmdline.tools.MockCommandLinePrompt;
import com.dslplatform.compiler.client.cmdline.tools.TestArguments;
import com.dslplatform.compiler.client.cmdline.tools.TestingOutput;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class DeployUnmanagedServerTest {

    @Test
    public void testDeployUnmanagedServerTest() throws IOException {
        Logger logger = LoggerFactory.getLogger("dcc-test");
        Arguments arguments = new TestArguments("/deploy_unmanaged_server.props", logger);
        CommandLinePrompt clcp = new MockCommandLinePrompt(true, true, true, true, true, true, true, true, true, true);
        Api api = new ApiImpl(new HttpRequestBuilderImpl(), new HttpTransportMock(), UnmanagedDSLMock.mock_single_integrated);
        TestingOutput output = new TestingOutput();
        CLCAction action = new ActionDefinition(api, logger, output, arguments, clcp, new ClcIO(logger));

        Main.processArguments(action, arguments);
        assertTrue(arguments.getCompilationTargetPath().compilationTargetPath.exists());
        assertTrue(arguments.getMigrationFilePath().migrationFilePath.exists());
    }

    @Test
    public void testDeployUnmanagedServerTestAndCopy() throws IOException {
        Logger logger = LoggerFactory.getLogger("dcc-test");
        Arguments arguments = new TestArguments("/deploy_unmanaged_server.props", logger);
        CommandLinePrompt clcp = new MockCommandLinePrompt(true, true, true, true, true, true, true, true, true, true);
        Api api = new ApiImpl(new HttpRequestBuilderImpl(), new HttpTransportMock(), UnmanagedDSLMock.mock_single_integrated);
        TestingOutput output = new TestingOutput();
        CLCAction action = new ActionDefinition(api, logger, output, arguments, clcp, new ClcIO(logger));

        Main.processArguments(action, arguments);;
        assertTrue(arguments.getMonoApplicationPath().monoApplicationPath.exists());
    }
}
