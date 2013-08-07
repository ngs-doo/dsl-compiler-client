package com.dslplatform.compiler.client.cmdline.processor;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;

import com.dslplatform.compiler.client.api.commons.FileLoader;
import com.dslplatform.compiler.client.api.commons.Hash;
import com.dslplatform.compiler.client.api.commons.PathExpander;
import com.dslplatform.compiler.client.api.commons.io.FileUtils;
import com.dslplatform.compiler.client.api.commons.io.IOUtils;
import com.dslplatform.compiler.client.api.diff.HashBodyMapTool;
import com.dslplatform.compiler.client.api.diff.PathAction;
import com.dslplatform.compiler.client.api.params.Arguments;
import com.dslplatform.compiler.client.api.params.ProjectID;
import com.dslplatform.compiler.client.io.Prompt;
import com.dslplatform.compiler.client.io.Logger;

public class BaseHandler {
    private final Logger logger;
    private final Prompt prompt;
    private final PathExpander pathExpander;

    private final static char slash = IOUtils.DIR_SEPARATOR;
    private final static boolean isNotLinux = slash != '/';

    protected BaseHandler(
            final Logger logger,
            final Prompt prompt) {
        this.logger = logger;
        this.prompt = prompt;
        this.pathExpander = new PathExpander(logger);
    }

    protected void updateFiles(
            final Arguments arguments,
            final SortedMap<String, byte[]> fileBodies) throws IOException {

        final File op = arguments.getOutputPath();
        if (!op.exists()) op.mkdirs();

        final Map<Hash, SortedSet<String>> oldHash= new FileLoader(logger)
            .addPath(op.getPath())
            .getHashBodyMap();

        final FileLoader newFilesLoader = new FileLoader(logger);

        for(final Map.Entry<String, byte[]> entry : fileBodies.entrySet()) {
            final String filename = cleanFilename(entry.getKey());
            if (!isProjectIni(filename)) {
                newFilesLoader.addBytes(filename, entry.getValue());
            }
        }

        final Map<Hash, SortedSet<String>> newHash = newFilesLoader.getHashBodyMap();
        final Map<String, PathAction> actions = HashBodyMapTool.compareHashBodyMaps(oldHash, newHash);
        final SortedMap<String, byte[]> newFiles = newFilesLoader.getBodies();

        for (final Map.Entry<String, PathAction> hashAction : actions.entrySet()) {
            final String hash = hashAction.getKey();
            final PathAction action = hashAction.getValue();

            final File source = action.source == null ? null : new File(op, action.source);
            final File destination = action.destination == null ? null : new File(op, action.destination);

            logger.debug(
                "Preforming action: " + action.action.name() + " on file " + source +
                (destination == null ? "" : " -> dest:" + destination));

            switch (action.action) {
                case NO_CHANGE:
                case SKIPPED:
                case CREATED_DIR:
                    break;

                case CREATED:
                case MODIFIED: {
                    final byte[] body = newFiles.get(hash);
                    logger.debug(""+body.length);
                    FileUtils.writeByteArrayToFile(source, body);
                    break;
                }

                case MOVED:
                    FileUtils.moveFile(source, destination);
                    break;

                case COPY:
                    FileUtils.copyFile(source, destination);
                    break;

                case DELETED_DIR:
                case DELETED:
                    FileUtils.deleteQuietly(source);
                    break;
            }
        }

        final File projectIniPath = arguments.getProjectIniPath();
        updateProjectIni(fileBodies, projectIniPath);
    }

    private boolean updateProjectIni(
            final SortedMap<String, byte[]> fileBodies,
            final File projectIniPath) {

        if (projectIniPath == null) return false;

        logger.debug("About to replace project ini " + projectIniPath );

        final Map.Entry<String, byte[]> pib = findProjectIni(fileBodies);
        if ( pib == null ) {
            logger.debug("Java project.ini not found in generated sources.");
            return false;
        }
        logger.debug("New Project ini found " + pib.getKey());

        try {
            FileUtils.writeByteArrayToFile(projectIniPath, pib.getValue());
        } catch (IOException e) {
            logger.debug("Error writing new project ini" + e.getMessage());
            return false;
        }

        logger.debug("Project ini updated: " + new String(pib.getValue(), Charset.forName("UTF-8")));
        return true;
    }

    private boolean isProjectIni(final String path){
        return path.endsWith("ava/project.ini");
    }

    private Map.Entry<String, byte[]> findProjectIni(final Map<String, byte[]>fileBodies) {
        for(final Map.Entry<String, byte[]> file: fileBodies.entrySet()) {
            if (isProjectIni(file.getKey())) return file;
        }

        return null;
    }

    public static String cleanFilename(final String path) {
        if (path == null) return null;
        final String slashslash = "" + slash + slash;
        final String singleslash = "" + slash;
        if (path.startsWith(singleslash)) return cleanFilename(path.substring(1));
        if (path.startsWith("/")) return cleanFilename(path.substring(1));
        return  ((isNotLinux) ?
                      path.replace('/', slash) : path).replace(slashslash, singleslash);
        }
}
