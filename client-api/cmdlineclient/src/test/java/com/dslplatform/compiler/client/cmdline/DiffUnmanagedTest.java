package com.dslplatform.compiler.client.cmdline;

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
import java.io.IOException;

import static org.junit.Assert.*;

public class DiffUnmanagedTest {

    @Test
    public void testDiffUnmanagedTest() throws IOException {
        Logger logger = LoggerFactory.getLogger("dcc-test");
        Arguments arguments = new TestArguments("/diff_unmanaged_test.props", logger);
        CommandLinePrompt clcp = new MockCommandLinePrompt(true, true, true, true);
        IOMock io = new IOMock();
        Api api = new ApiImpl(new HttpRequestBuilderImpl(), new HttpTransportMock(), UnmanagedDSLMock.mock_single_integrated);
        TestingOutput output = new TestingOutput();
        CLCAction action = new ActionDefinition(api, logger, output, arguments, clcp, io);

        Main.processArguments(action, arguments);
        assertTrue(output.acc.toString().contains("Created 2.dsl"));
    }
}
