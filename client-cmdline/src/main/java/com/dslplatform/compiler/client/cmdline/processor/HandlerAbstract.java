package com.dslplatform.compiler.client.cmdline.processor;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.dslplatform.compiler.client.api.commons.FileLoader;
import com.dslplatform.compiler.client.api.commons.Hash;
import com.dslplatform.compiler.client.api.diff.HashBodyMapTool;
import com.dslplatform.compiler.client.api.diff.PathAction;
import com.dslplatform.compiler.client.api.logging.Logger;
import com.dslplatform.compiler.client.api.params.Arguments;
import com.dslplatform.compiler.client.api.params.ProjectID;
import com.dslplatform.compiler.client.cmdline.prompt.Prompt;

public class HandlerAbstract {
    private final Logger logger;
    private final Prompt prompt;

    private static char slash = IOUtils.DIR_SEPARATOR;

    protected HandlerAbstract(
            final Logger logger,
            final Prompt prompt) {
        this.logger = logger;
        this.prompt = prompt;
    }

    protected ProjectID getOrPromptProjectID(
            final Arguments arguments) {
        final ProjectID pid = arguments.getProjectID();
        if (pid != null) return pid;
        logger.info("ProjectID not found in arguments, prompting for projectID.");
        return new ProjectID(
                java.util.UUID.fromString(
                        prompt.readLine("ProjectID:", null)));
    }

    protected String getOrPromptOutputPath(
            final Arguments arguments) {
        final String outputpath = arguments.getOutputPath();
        if (outputpath !=  null) return outputpath;
        logger.info("Output path not found in arguments, prompting.");
        return prompt.readLine("Output Path:", null);
    }

    protected static void writeDiff(
            final Logger logger,
            final String outputPath,
            final Map<String, byte[]> fileBodies) throws IOException {

        final File op = new File(outputPath);
        if (!op.exists()) op.mkdirs();

        final Map<Hash, SortedSet<String>> oldHash= new FileLoader(logger)
            .addPath(outputPath)
            .getHashBodyMap();

        final FileLoader newFilesLoader = new FileLoader(logger);

        for(final Map.Entry<String, byte[]> entry : fileBodies.entrySet()) {
            newFilesLoader.addBytes(entry.getKey(), entry.getValue());
        }

        final Map<Hash, SortedSet<String>> newHash = newFilesLoader.getHashBodyMap();
        final Map<String, PathAction> actions = HashBodyMapTool.compareHashBodyMaps(oldHash, newHash);
        final SortedMap<String, byte[]> newFiles = newFilesLoader.getBodies();

        for (final Map.Entry<String, PathAction> hashaction : actions.entrySet()) {
            final String hash = hashaction.getKey();
            final PathAction action = hashaction.getValue();

            final String opSlash = outputPath + slash;
            final String source = action.source != null ? opSlash + action.source : null;
            final String destination = action.destination != null ? opSlash + action.destination : null;
            logger.trace(
                "Preforming action: " + action.action.name() +
                " on file " + source + " dest:" +
                (destination != null ? destination : "null" ));

            switch (action.action) {
                case NO_CHANGE:
                case SKIPPED:
                case CREATED_DIR:
                    break;

                case CREATED:
                case MODIFIED: {
                    final byte[] body = newFiles.get(hash);
                    FileUtils.writeByteArrayToFile(new File(source), body);
                    break;
                }

                case MOVED:
                    FileUtils.moveFile(new File(source), new File(destination));
                    break;

                case COPY:
                    FileUtils.copyFile(new File(source), new File(destination));
                    break;

                case DELETED_DIR:
                case DELETED:
                    FileUtils.deleteQuietly(new File(source));
                    break;
            }
        }
    }
}
