package com.dslplatform.compiler.client.cmdline.processor;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

import com.dslplatform.compiler.client.api.commons.FileLoader;
import com.dslplatform.compiler.client.api.commons.Hash;
import com.dslplatform.compiler.client.api.commons.io.FileUtils;
import com.dslplatform.compiler.client.api.diff.HashBodyMapTool;
import com.dslplatform.compiler.client.api.diff.PathAction;
import com.dslplatform.compiler.client.api.params.Arguments;
import com.dslplatform.compiler.client.io.Logger;
import com.dslplatform.compiler.client.io.Output;
import com.dslplatform.compiler.client.io.Prompt;

public class BaseHandler {
    private final Logger logger;

//    private final Prompt prompt;
    private final Output output;

    /*
      Used dir_separator is / since it works on all platforms.
      If this should change than code replacing / with platform dependent
      separator should be moved to server side.
    */

    protected BaseHandler(
            final Logger logger,
            final Prompt prompt,
            final Output output) {
        this.logger = logger;
//        this.prompt = prompt;
        this.output = output;
    }

    protected void updateFiles(
            final Arguments arguments,
            final SortedMap<String, byte[]> fileBodies,
            final File outputPath) throws IOException {

        final File op = arguments.getOutputPath();
        if (!op.exists()) {
            op.mkdirs();
        }

        final Set<String> languagePaths = new LinkedHashSet<String>();
        for (final String path : fileBodies.keySet()) {
            languagePaths.add(path.replaceFirst("/.*", ""));
        }

        final FileLoader fileLoader = new FileLoader(logger);
        for (final String path : languagePaths) {
            final String language = path.replaceFirst("/.*", "");
            final File languagePath = new File(op.getPath(), language);

            if (languagePath.isDirectory()) {
                logger.trace("Marking managed directory: " + languagePath);
                fileLoader.addPath(op.getPath(), languagePath.getPath());
            }
        }

        final Map<Hash, SortedSet<String>> oldHash = fileLoader
                .getHashBodyMap();

        final FileLoader newFilesLoader = new FileLoader(logger);

        for (final Map.Entry<String, byte[]> entry : fileBodies.entrySet()) {
            newFilesLoader.addBytes(cleanFilename(entry.getKey()),
                    entry.getValue());
        }

        final Map<Hash, SortedSet<String>> newHash = newFilesLoader
                .getHashBodyMap();
        final Map<String, PathAction> actions = HashBodyMapTool
                .compareHashBodyMaps(oldHash, newHash);
        final SortedMap<String, byte[]> newFiles = newFilesLoader.getBodies();

        for (final Map.Entry<String, PathAction> hashAction : actions
                .entrySet()) {
            final String hash = hashAction.getKey();
            final PathAction action = hashAction.getValue();

            final File source = action.source == null ? null : new File(op,
                    action.source);

            final File destination = action.destination == null
                    ? null
                    : new File(op, action.destination);

            logger.debug("Performing action: " + action.action.name()
                    + " on file " + source
                    + (destination == null ? "" : " -> dest:" + destination));

            switch (action.action) {
                case NO_CHANGE:
                case SKIPPED:
                case CREATED_DIR:
                    break;

                case CREATED:
                case MODIFIED: {
                    final byte[] body = newFiles.get(hash);
                    FileUtils.writeByteArrayToFile(source, body);
                    break;
                }

                case MOVED:
                    try {
                        FileUtils.moveFile(source, destination);
                    } catch (final IOException e) {
                        if (source.getCanonicalPath().equalsIgnoreCase(
                                destination.getCanonicalPath())
                                && System.getProperty("os.name").contains(
                                        "Windows")) {
                            logger.debug(String
                                    .format("Could not move \"%s\" to \"%s\" (Windows is case insensitive)",
                                            source, destination));
                        } else {
                            throw e;
                        }
                    }
                    checkIfParentEmptyAndDelete(source, outputPath);
                    break;

                case COPY:
                    FileUtils.copyFile(source, destination);
                    break;

                case DELETED_DIR:
                case DELETED:
                    FileUtils.deleteQuietly(source);
                    checkIfParentEmptyAndDelete(source, outputPath);
                    break;
            }
        }
    }

    private void checkIfParentEmptyAndDelete(
            final File source,
            final File outputPath) {
        final File parent = source.getParentFile();
        if (parent.list().length == 0 && !parent.equals(outputPath)) {
            FileUtils.deleteQuietly(parent);
            checkIfParentEmptyAndDelete(parent, outputPath);
        }
    }

    protected void updateProjectIni(
            final File projectIniPath,
            final byte[] projectIni) throws IOException {
        if (projectIniPath == null) {
            output.println("No output file specified, copy paste the following into your dsl-project.ini:\n");
            output.println(new String(projectIni, "UTF-8"));
        } else {
            if (projectIniPath.exists()) {
                final byte[] body = FileUtils
                        .readFileToByteArray(projectIniPath);
                if (Arrays.equals(body, projectIni)) {
                    logger.debug("Skipping project configuration file because it is identical ...");
                    return;
                } else {
                    logger.debug("About to overwrite the project configuration file ...");
                    output.println("Overwriting project configuration file: "
                            + projectIniPath);
                }
            } else {
                logger.debug("Creating new project configuration file ...");
                output.println("Creating new project configuration: "
                        + projectIniPath.getPath());
            }

            FileUtils.writeByteArrayToFile(projectIniPath, projectIni);
        }
    }

    // Only used to clean unix filename from double and starting slash
    private static String cleanFilename(final String path) {
        if (path == null) {
            return null;
        }
        if (path.startsWith("/")) {
            return cleanFilename(path.substring(1));
        }
        return path.replace("//", "/");
    }
}
