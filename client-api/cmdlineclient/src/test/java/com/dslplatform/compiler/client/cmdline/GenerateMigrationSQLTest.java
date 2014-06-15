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

public class GenerateMigrationSQLTest {

    @Test
    public void testGenerateMigrationSQL() throws IOException {
        Logger logger = LoggerFactory.getLogger("dcc-test");
        Arguments arguments = new TestArguments("/generate_sql_migration.props", logger);
        CommandLinePrompt clcp = new MockCommandLinePrompt(true, true, true, true);
        IOMock io = new IOMock();
        Api api = new ApiImpl(new HttpRequestBuilderImpl(), new HttpTransportMock(), UnmanagedDSLMock.mock_single_integrated);
        TestingOutput output = new TestingOutput();
        CLCAction action = new ActionDefinition(api, logger, output, arguments, clcp, io);

        action.sqlMigration();
        Assert.assertTrue("Migration file was not written to test output!",
                io.contains(IOMock.MockedAction.Write, arguments.getMigrationFilePath().migrationFilePath));
    }
}
