package com.dslplatform.compiler.client.cmdline;

import com.dslplatform.compiler.client.cmdline.parser.Arguments;
import com.dslplatform.compiler.client.cmdline.tools.TestArguments;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class DeployUnmanagedServerTest extends DCCTest {

    protected Arguments makeArguments() {
        return TestArguments.make("/deploy_unmanaged_server.props", logger);
    }

    @Test
    public void testDeployUnmanagedServerTest() throws IOException {
        assertTrue(arguments.getCompilationTargetPath().compilationTargetPath.exists());
        assertTrue(arguments.getMigrationFilePath().migrationFilePath.exists());

        final File monoApplicationPath = arguments.getMonoApplicationPath().monoApplicationPath;
        final File monoBin = new File(monoApplicationPath, "bin");
        final File revenjExe = new File(monoBin, "Revenj.Http.exe");
        final File generatedModel = new File(monoBin, "generatedModel.dll");
        final File startSh = new File(monoApplicationPath, "start.sh");
        final File logsDir = new File(monoApplicationPath, "logs");
        final File cacheDir = new File(monoApplicationPath, "cache");

        assertTrue("Mono folder is missing!", monoBin.exists());
        assertTrue("Revenj Exe is missing!", revenjExe.exists());
        assertTrue("Generated model is missing!", generatedModel.exists());
        assertTrue("start.sh is missing!", startSh.exists());
        assertTrue("Cache is missing!", cacheDir.exists());
        assertTrue("Logs is missing!", logsDir.exists());

        /** todo - Run the service and check the output.*/
    }
}
