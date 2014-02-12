package com.dslplatform.compiler.client.api.commons;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import com.dslplatform.compiler.client.api.commons.io.FileUtils;
import com.dslplatform.compiler.client.api.commons.io.IOUtils;
import com.dslplatform.compiler.client.io.Logger;

public class FileLoader {
    /** Used to skip already loaded files */
    private final SortedMap<String, Hash.Body> fileBodies = new TreeMap<String, Hash.Body>();

    /** Used not to allocate buffers for files with identical contents */
    private final Map<Hash, SortedSet<String>> hashBodies = new HashMap<Hash, SortedSet<String>>();

    private final static char slash = IOUtils.DIR_SEPARATOR;
    // -------------------------------------------------------------------------

    private final Logger logger;

    public FileLoader(
            final Logger logger) {
        this.logger = logger;
    }

    // -------------------------------------------------------------------------

    private static final Pattern DEFAULT_PATTERN = Pattern.compile(".*");

    public FileLoader addPath(final String path) throws IOException {
        return addPath(path, DEFAULT_PATTERN);
    }

    public FileLoader addPath(final String path, final Pattern pattern)
            throws IOException {
        return addPath(path, path, pattern);
    }

    public FileLoader addPath(final String rootPath, final String path)
            throws IOException {
        return addPath(rootPath, path, DEFAULT_PATTERN);
    }

    public FileLoader addPath(
            final String rootPath,
            final String path,
            final Pattern pattern) throws IOException {
        logger.debug("Adding file: " + path);
        final File file = new File(path);

        if (file.isDirectory()) {
            logger.trace("Recursively adding files in directory \"" + file
                    + "\" via pattern: " + pattern);
            for (final String curPath : file.list()) {
                addPath(rootPath, path + "/" + curPath, pattern);
            }
        } else if (file.isFile()) {
            final String realPath = file.getCanonicalPath();
            if (pattern.matcher(realPath).matches()) {
                addFile(rootPath, realPath, file);
            }
        } else {
            throw new IOException("File or directory \"" + file
                    + "\" does not exist!");
        }

        return this;
    }

    private void addFile(
            final String rootPath,
            final String realPath,
            final File file) throws IOException {
        if (fileBodies.containsKey(realPath)) {
            logger.debug("File \"" + realPath
                    + "\" already added, skipping ...");
            return;
        }

        logger.trace("Reading file: " + realPath);
        final byte[] content = FileUtils.readFileToByteArray(file);

        final File rootCanonicalPath = new File(rootPath);

        // TODO: fixes case when user specifies full path to file, screams for refactoring.
        final String relativePath = rootCanonicalPath.isDirectory()
                ? realPath.substring(rootCanonicalPath.getCanonicalPath()
                        .length() + 1) : rootCanonicalPath.getName();
        addBytes(relativePath, content);
    }

    public void addBytes(final String rawrealPath, final byte[] content) {
        synchronized (fileBodies) {
            // All files with path separator /
            final String realPath = slash == '/' ? rawrealPath : rawrealPath
                    .replace(slash, '/');

            if (fileBodies.containsKey(realPath)) {
                return;
            }

            final Hash.Body body = new Hash.Body(content);
            SortedSet<String> files = hashBodies.get(body);
            if (files == null) {
                files = new TreeSet<String>();
                hashBodies.put(body, files);
            }
            files.add(realPath);

            fileBodies.put(realPath, body);
            logger.debug("Added path to the file bodies: " + realPath);
        }
    }

    // -------------------------------------------------------------------------

    public SortedMap<String, byte[]> getBodies() {
        final SortedMap<String, byte[]> bodies = new TreeMap<String, byte[]>();
        synchronized (fileBodies) {
            for (final Map.Entry<String, Hash.Body> entry : fileBodies
                    .entrySet()) {
                bodies.put(entry.getKey(), entry.getValue().body);
            }
        }
        return bodies;
    }

    // -------------------------------------------------------------------------

    public Map<Hash, SortedSet<String>> getHashBodyMap() {
        final Map<Hash, SortedSet<String>> hashBodyMap = new LinkedHashMap<Hash, SortedSet<String>>();
        synchronized (fileBodies) {
            for (final Hash body : fileBodies.values()) {
                hashBodyMap.put(body, hashBodies.get(body));
            }
        }
        return hashBodyMap;
    }
}
