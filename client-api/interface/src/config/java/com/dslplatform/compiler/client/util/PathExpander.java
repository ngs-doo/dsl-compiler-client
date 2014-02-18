package com.dslplatform.compiler.client.util;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;

public class PathExpander {
    private final Logger logger;
    private final File userHome;

    public PathExpander(final Logger logger) {
        this.logger = logger;
        userHome = getUserHome();
        logger.trace("User home set to: " + userHome);
    }

    private File canonify(final String path) {
        final File file = new File(path);
        try {
            return file.getCanonicalFile();
        } catch (final IOException e) {
            logger.warn("Could not canonify file: {}", file);
            return file.getAbsoluteFile();
        }
    }

    public File getUserHome() {
        return canonify(System.getProperty("user.home"));
    }

    public File expandPath(final String path) {
        if (path.charAt(0) == '~') {
            final File expandedPath = new File(userHome, path.substring(1));
            logger.trace("Expanding path: " + expandedPath);
            return expandedPath;
        } else return canonify(path);
    }
}
