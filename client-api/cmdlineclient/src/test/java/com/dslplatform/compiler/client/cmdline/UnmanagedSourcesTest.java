package com.dslplatform.compiler.client.cmdline;

import com.dslplatform.compiler.client.cmdline.parser.Arguments;
import com.dslplatform.compiler.client.cmdline.tools.TestArguments;
import org.junit.AfterClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class UnmanagedSourcesTest extends DCCTest {

    protected Arguments makeArguments() {
        return TestArguments.make("/unmanaged_sources_test.props", logger);
    }

    @Test
    public void testUnmanagedSourcesTest() throws IOException {
        assertTrue("C# Converter file missing!", new File(arguments.getOutputPath().outputPath, "csharpserver/_DatabaseCommon/FactorymyModule_A/AConverter.cs").exists());
        assertTrue("AssemblyInfo.cs missing!", new File(arguments.getOutputPath().outputPath, "csharpserver/AssemblyInfo.cs").exists());
        assertTrue("Java file missing!", new File(arguments.getOutputPath().outputPath, "java/namespace/myModule/B.java").exists());
    }


    @AfterClass
    public static void tearDown() throws IOException {
        /** Clean up.*/
    }
}
