package com.dslplatform.compiler.client;

import com.dslplatform.compiler.client.diff.HashBodyMapTool;
import com.dslplatform.compiler.client.diff.PathAction;
import com.dslplatform.compiler.client.io.DSLLoader;
import com.dslplatform.compiler.client.io.FileLoader;
import com.dslplatform.compiler.client.io.Hash;
import com.dslplatform.compiler.client.params.DSL;
import com.dslplatform.compiler.client.response.Source;
import org.apache.commons.codec.Charsets;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

public class IO {
    public static void write(File file, byte[] content) throws IOException {
        FileUtils.writeByteArrayToFile(file, content);
    }

    public static void write(File file, String content, Charset encoding) throws IOException {
        FileUtils.write(file, content, encoding);
    }

    public static void move(File fromFile, File toFile) throws IOException {
        FileUtils.moveFile(fromFile, toFile);
    }

    public static void copy(File fromFile, File toFile) throws IOException {
        FileUtils.copyFile(fromFile, toFile);
    }

    public static void copyToDir(File fromFile, File toFile) throws IOException {
        if (fromFile.isDirectory()) FileUtils.copyDirectory(fromFile, toFile);
        else FileUtils.copyFile(fromFile, new File(toFile, fromFile.getName()));
    }

    public static void delete(File fileToDelete) {
        FileUtils.deleteQuietly(fileToDelete);
    }

    public static DSL readDSL(File from) throws IOException {
        return new DSL(new DSLLoader(Charsets.UTF_8).addPath(from.getAbsolutePath()).getDSL());
    }

    public static void mkdirs(File dir) throws IOException {
        dir.mkdirs();
    }

    /**
     * This method will first look into received sources to see what languages are present in the list
     * only the languages which are present in will be managed.
     */
    public static void updateFiles(
            final Logger logger,
            final List<Source> fileBodies,
            final File outputPath) throws IOException {

        if (!outputPath.exists()) {
            mkdirs(outputPath);
        }

        final Set<String> languagePaths = new LinkedHashSet<String>();
        for (final Source source : fileBodies) {
            languagePaths.add(source.language);
        }

        final FileLoader fileLoader = new FileLoader();
        for (final String path : languagePaths) {
            final File languagePath = new File(outputPath.getPath(), path);

            if (languagePath.isDirectory()) {
                logger.trace("Marking managed directory: {}", languagePath);
                fileLoader.addPath(outputPath.getPath(), languagePath.getPath());
            }
        }

        final Map<Hash, SortedSet<String>> oldHash = fileLoader
                .getHashBodyMap();

        final FileLoader newFilesLoader = new FileLoader();

        for (final Source source : fileBodies) {
            String rawRealPath = cleanFilename(source.language + "/" + source.path);
            newFilesLoader.addBytes(rawRealPath, source.content);
        }

        final Map<Hash, SortedSet<String>> newHash = newFilesLoader.getHashBodyMap();
        final Map<String, PathAction> actions = HashBodyMapTool.compareHashBodyMaps(oldHash, newHash);
        final SortedMap<String, byte[]> newFiles = newFilesLoader.getBodies();

        for (final Map.Entry<String, PathAction> hashAction : actions.entrySet()) {
            final String hash = hashAction.getKey();
            final PathAction action = hashAction.getValue();

            final File source = action.source == null ? null : new File(outputPath, action.source);

            final File destination = action.destination == null
                    ? null
                    : new File(outputPath, action.destination);

            logger.debug("Performing action: {} on file {} {}", action.action.name(), source, (destination == null ? "" : " -> dest:" + destination));

            switch (action.action) {
                case NO_CHANGE:
                case SKIPPED:
                case CREATED_DIR:
                    break;

                case CREATED:
                case MODIFIED:
                    write(source, newFiles.get(hash));
                    break;

                case MOVED:
                    try {
                        move(source, destination);
                    } catch (final IOException e) {
                        if (source.getCanonicalPath().equalsIgnoreCase(
                                destination.getCanonicalPath())
                                && System.getProperty("os.name").contains(
                                "Windows")) {
                            logger.debug(String
                                    .format("Could not move \"%s\" to \"%s\" (Windows is case insensitive)",
                                            source, destination));
                        } else throw e;
                    }
                    checkIfParentEmptyAndDelete(source, outputPath);
                    break;

                case COPY:
                    copy(source, destination);
                    break;

                case DELETED_DIR:
                case DELETED:
                    delete(source);
                    checkIfParentEmptyAndDelete(source, outputPath);
                    break;
            }
        }
    }

    private static void checkIfParentEmptyAndDelete(
            final File source,
            final File outputPath) {
        final File parent = source.getParentFile();
        if (parent.list().length == 0 && !parent.equals(outputPath)) {
            delete(parent);
            checkIfParentEmptyAndDelete(parent, outputPath);
        }
    }

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
