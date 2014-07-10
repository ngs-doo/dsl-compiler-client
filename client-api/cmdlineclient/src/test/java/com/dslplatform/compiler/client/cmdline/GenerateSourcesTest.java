package com.dslplatform.compiler.client.cmdline;

import com.dslplatform.compiler.client.Api;
import com.dslplatform.compiler.client.ApiImpl;
import com.dslplatform.compiler.client.api.core.impl.HttpRequestBuilderImpl;
import com.dslplatform.compiler.client.api.core.mock.HttpTransportMock;
import com.dslplatform.compiler.client.api.core.mock.UnmanagedDSLMock;
import com.dslplatform.compiler.client.cmdline.parser.Arguments;
import com.dslplatform.compiler.client.cmdline.tools.TestArguments;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class GenerateSourcesTest extends DCCTest {

    protected Arguments makeArguments() {
        return TestArguments.make("/generate_managed_test.props", logger);
    }

    public Api makeApi() {
        return new ApiImpl(new HttpRequestBuilderImpl(), new HttpTransportMock(), UnmanagedDSLMock.mock_single_integrated);
    }

    @Test
    public void generateSourcesTest() throws IOException {
        assertTrue(arguments.getOutputPath().outputPath.exists());
        assertTrue("missing BRepository.java.", new java.io.File(arguments.getOutputPath().outputPath, "/java/namespace/A/repositories/BRepository.java").exists());
    }
}
