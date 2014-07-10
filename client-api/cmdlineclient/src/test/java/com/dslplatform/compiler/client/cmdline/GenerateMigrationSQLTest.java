package com.dslplatform.compiler.client.cmdline;

import com.dslplatform.compiler.client.cmdline.parser.Arguments;
import com.dslplatform.compiler.client.cmdline.tools.TestArguments;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class GenerateMigrationSQLTest extends DCCTest {

    protected Arguments makeArguments() {
        return TestArguments.make("/generate_sql_migration.props", logger);
    }

    @Test
    public void testGenerateMigrationSQL() throws IOException {
        Assert.assertTrue("Migration file was not written to the test output!", arguments.getMigrationFilePath().migrationFilePath.exists());
    }
}
