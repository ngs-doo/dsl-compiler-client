package com.dslplatform.compiler.client.api.commons;

import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;

public class PathExpander {
    private final Logger logger;
    private final File userHome;

    public PathExpander(
            final Logger logger) {
        this.logger = logger;
        userHome = getUserHome();

        logger.debug("User home set to: " + userHome);
    }

    public File getUserHome() {
        final File userHome = new File(System.getProperty("user.home"));
        try {
            return userHome.getCanonicalFile();
        } catch (final IOException e) {
            logger.warn("Could not find canonical path of: " + userHome);
            return userHome.getAbsoluteFile();
        }
    }

    public File expandPath(final String path) {
        if (path.charAt(0) == '~') {
            final File expandedPath = new File(userHome, path.substring(1));
            logger.trace("Expanding path: " + expandedPath);
            return expandedPath;
        } else {
            return new File(path);
        }
    }
}
