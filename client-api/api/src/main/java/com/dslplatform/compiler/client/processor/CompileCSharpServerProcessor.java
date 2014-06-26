package com.dslplatform.compiler.client.processor;

import com.dslplatform.compiler.client.response.CompileCSharpServerResponse;
import org.apache.commons.codec.Charsets;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

import java.io.*;

/**
 * Created by marin on 07.07.14..
 */
public class CompileCSharpServerProcessor {

    private static final String compilationMessage = "Compilation successful";

    public CompileCSharpServerProcessor(Logger logger, File sourcePath, File dependencies, File target) {
        this.logger = logger;
        this.sourcePath = sourcePath;
        this.dependencies = dependencies;
        this.target = target;
    }

    final Logger logger;
    final File sourcePath;
    final File dependencies;
    final File target;

    public CompileCSharpServerResponse process() {
        final String runScript = makeRunScript(sourcePath, dependencies, target);
        writeRunScript(runScript, target);
        Process process = null;
        logger.trace("About to run mcs script");
        logger.trace(runScript);
        try {
            process = Runtime.getRuntime().exec(runScript);
            final Process finalProcess = process;

            /* Consume output stream */
            new Thread() {
                public void run() {
                    final InputStream in = finalProcess.getInputStream();
                    try {
                        final InputStreamReader inputStreamReader = new InputStreamReader(in);
                        final BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                        String line;
                        while ((line = bufferedReader.readLine()) != null) logger.trace(line);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        close(in);
                    }
                }
            }.start();

            /* Consume error stream */
            new Thread() {
                public void run() {
                    final InputStream in = finalProcess.getErrorStream();
                    try {
                        final InputStreamReader inputStreamReader = new InputStreamReader(in);
                        final BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                        String line;
                        while ((line = bufferedReader.readLine()) != null) logger.error(line);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        close(in);
                    }
                }
            }.start();
            process.waitFor();

            return new CompileCSharpServerResponse(true, compilationMessage);
        } catch (IOException e) {
            e.printStackTrace();
            return new CompileCSharpServerResponse(false, e.getMessage());
        } catch (InterruptedException e) {
            return new CompileCSharpServerResponse(false, e.getMessage());
        } finally {
            if (process != null) {
                try {
                    process.getOutputStream().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                process.destroy();
            }
        }
    }

    private static void close(final InputStream inputStream) {
        try {
            if (inputStream != null) inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String makeRunScript(File sourcePath,
                                        File revenj,
                                        File target) {
        if (sourcePath == null) throw new IllegalArgumentException("Must provide the path to the sources.");
        if (revenj == null) throw new IllegalArgumentException("Must provide the path to revenj.");
        if (target == null) throw new IllegalArgumentException("Must provide the path to compilation target.");

        StringBuilder sb = new StringBuilder("mcs -v");
        String[] systemDependencies = {
                "System.ComponentModel.Composition",
                "System",
                "System.Data",
                "System.Xml",
                "System.Runtime.Serialization",
                "System.Configuration",
                "System.Drawing"
        };

        final String targetOutputPath = target.getPath();
        sb.append(" -out:").append(targetOutputPath)
                .append(" -target:library")
                .append(" -lib:").append(revenj.getPath());
        for (String systemDependency : systemDependencies)
            addCSCompileDependency(sb, systemDependency);
        File[] revenjFiles = revenj.listFiles();
        for (File dependency : revenjFiles) /* todo - dereference may cause NPE, unable to test. */
            if (dependency.getName().endsWith(".dll"))
                addCSCompileDependency(sb, dependency.getName());
        sb.append(" -recurse:").append(sourcePath.getAbsolutePath()).append("/*.cs");
        return sb.toString();
    }

    private static void addCSCompileDependency(StringBuilder sb, String dependency) {
        sb.append(" -r:").append(dependency);
    }

    /** temp function for debugging */
    private void writeRunScript(final String runScript, final File target) {
        final File runScriptPath = new File(target.getParent(), "tmp/runScript");
        try {
            FileUtils.write(runScriptPath, runScript, Charsets.UTF_8);
        } catch (IOException e) {
            logger.trace("Unable to write runScript");
        }
    }

}
