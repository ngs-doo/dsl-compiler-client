package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.TestUtils;
import org.junit.Assert;
import org.junit.Test;

public class DslCompilerMonoRuntimeDetectionTest {

    @Test
    public void testSegmentationFault() {
        Assert.assertTrue(DslCompiler.monoNativeFailure((TestUtils.fileContent("/segmentationFault.txt"))));
    }

    @Test
    public void testInvalidILCode() {
        Assert.assertTrue(DslCompiler.monoNativeFailure(TestUtils.fileContent("/invalidILCode.txt")));
    }

    @Test
    public void testValidOutput() {
        Assert.assertFalse(DslCompiler.monoNativeFailure(TestUtils.fileContent("/validOutput.txt")));
    }
}
