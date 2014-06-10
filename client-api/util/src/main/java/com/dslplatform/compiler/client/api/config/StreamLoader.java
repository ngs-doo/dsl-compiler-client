package com.dslplatform.compiler.client.api.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;

import com.dslplatform.compiler.client.io.PathExpander;

public class StreamLoader {
    private final Logger logger;
    private final PathExpander pathExpander;

    public StreamLoader(final Logger logger, final PathExpander pathExpander) {
        this.logger = logger;
        this.pathExpander = pathExpander;
    }

    private InputStream openFromClasspath(final String path) throws IOException {
        final InputStream is = StreamLoader.class.getResourceAsStream(path);
        if (is == null) throw new IOException("Could not read stream from classpath: " + path);
        return is;
    }

    private InputStream openFromPath(final File path) throws IOException {
        try {
            return new FileInputStream(path);
        } catch (final IOException e) {
            throw new IOException("Could not read stream from path: " + path, e);
        }
    }

    public InputStream open(final String path) throws IOException {
        try {
            logger.trace("Attempting to open stream from classpath: {}", path);
            return openFromClasspath(path);
        } catch (final IOException e) {
            final File expandedPath = pathExpander.expandPath(path);
            logger.trace("Attempting to open stream from path: {}", expandedPath);
            return openFromPath(expandedPath);
        }
    }
}
