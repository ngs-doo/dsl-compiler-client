package com.dslplatform.compiler.client.cmdline;

import com.dslplatform.compiler.client.cmdline.parser.Arguments;
import com.dslplatform.compiler.client.cmdline.tools.TestArguments;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class DiffUnmanagedTest extends DCCTest {

    protected Arguments makeArguments() {
        return TestArguments.make("/diff_unmanaged_test.props", logger);
    }

    @Test
    public void testDiffUnmanagedTest() throws IOException {
        System.out.println(output.acc.toString());
        assertTrue(output.acc.toString().contains("Created 2.dsl"));
    }
}
