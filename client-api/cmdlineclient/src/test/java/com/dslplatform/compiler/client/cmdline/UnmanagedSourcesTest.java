package com.dslplatform.compiler.client.cmdline;

import static org.junit.Assert.*;
import com.dslplatform.compiler.client.Api;
import com.dslplatform.compiler.client.ApiImpl;
import com.dslplatform.compiler.client.api.core.impl.HttpRequestBuilderImpl;
import com.dslplatform.compiler.client.api.core.mock.HttpTransportMock;
import com.dslplatform.compiler.client.api.core.mock.UnmanagedDSLMock;
import com.dslplatform.compiler.client.cmdline.parser.Arguments;
import com.dslplatform.compiler.client.cmdline.tools.IOMock;
import com.dslplatform.compiler.client.cmdline.tools.MockCommandLinePrompt;
import com.dslplatform.compiler.client.cmdline.tools.TestArguments;
import com.dslplatform.compiler.client.cmdline.tools.TestingOutput;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class UnmanagedSourcesTest {

    @Test
    public void testUnmanagedSourcesTest() throws IOException {
        Logger logger = LoggerFactory.getLogger("dcc-test");
        Arguments arguments = new TestArguments("/unmanaged_sources_test.props", logger);
        CommandLinePrompt clcp = new MockCommandLinePrompt(true, true, true, true, true, true, true, true, true, true);
        IOMock io = new IOMock();
        Api api = new ApiImpl(new HttpRequestBuilderImpl(), new HttpTransportMock(), UnmanagedDSLMock.mock_single_integrated);
        TestingOutput output = new TestingOutput();
        CLCAction action = new ActionDefinition(api, logger, output, arguments, clcp, io);

        assertTrue(action.unmanagedSource());

        assertTrue(io.contains(IOMock.MockedAction.Write, new File(arguments.getOutputPath().outputPath, "_DatabaseCommon/FactorymyModule_A/AConverter.cs")));
        assertTrue(io.contains(IOMock.MockedAction.Write, new File(arguments.getOutputPath().outputPath, "AssemblyInfo.cs")));
        assertTrue(io.contains(IOMock.MockedAction.Write, new File(arguments.getOutputPath().outputPath, "namespace/myModule/B.java")));
    }
}
