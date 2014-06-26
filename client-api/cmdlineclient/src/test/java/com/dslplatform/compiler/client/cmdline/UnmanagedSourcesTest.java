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
import com.dslplatform.compiler.client.io.Output;
import com.dslplatform.compiler.client.response.Source;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

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

        assertTrue(io.contains(IOMock.MockedAction.Write, new File(arguments.getOutputPath().outputPath, "csharpserver/_DatabaseCommon/FactorymyModule_A/AConverter.cs")));
        assertTrue(io.contains(IOMock.MockedAction.Write, new File(arguments.getOutputPath().outputPath, "csharpserver/AssemblyInfo.cs")));
        assertTrue(io.contains(IOMock.MockedAction.Write, new File(arguments.getOutputPath().outputPath, "java/namespace/myModule/B.java")));
    }

    /** used to proxy to the updatedFiles method, to avoid putting more complexity in testing resources */
    static class TestActionDefinition extends ActionDefinition {
        public TestActionDefinition(Api api, Logger logger, Output output, Arguments arguments, CommandLinePrompt clp, IO io) {
            super(api, logger, output, arguments, clp, io);
        }

        public void updateEmpty() throws IOException {
            LinkedList<Source> fileBodies = new LinkedList<>();
            fileBodies.add(0, new Source("java", "test", new byte[]{}));
            fileBodies.add(1, new Source("csharpserver", "test", new byte[]{}));
            updateFiles(this.logger, fileBodies, arguments.getOutputPath().outputPath);
        }
    }

    @Test
    public void testUnmanagedSourcesManagementTest() throws IOException {
        Logger logger = LoggerFactory.getLogger("dcc-test");
        Arguments arguments = new TestArguments("/unmanaged_sources_test.props", logger);
        CommandLinePrompt clcp = new MockCommandLinePrompt(true, true, true, true, true, true, true, true, true, true);

        Api api = new ApiImpl(new HttpRequestBuilderImpl(), new HttpTransportMock(), UnmanagedDSLMock.mock_single_integrated);
        TestingOutput output = new TestingOutput();
        TestActionDefinition action = new TestActionDefinition(api, logger, output, arguments, clcp, new ClcIO(logger));

        assertTrue(action.unmanagedSource());

        File file11 = new File(arguments.getOutputPath().outputPath, "csharpserver/myModule/A.cs");
        File file12 = new File(arguments.getOutputPath().outputPath, "java/namespace/Guards.java");

        assertTrue(file11.exists());
        assertTrue(file12.exists());

        action.updateEmpty();

        assertTrue(!file11.exists());
        assertTrue(!file12.exists());
    }
}
