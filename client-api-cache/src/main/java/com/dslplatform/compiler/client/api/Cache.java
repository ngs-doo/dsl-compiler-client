package com.dslplatform.compiler.client.api;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import com.dslplatform.compiler.client.api.commons.HashUtil;
import com.dslplatform.compiler.client.api.logging.Logger;

import org.apache.commons.io.FileUtils;

public class Cache {
    private final Logger logger;

    private final File cachePath;
    private final File file;

    public Cache(final Logger logger, final String cachePath, final UUID projectID) {
        this.logger = logger;

        final String expandedCachePath;
        if (cachePath.charAt(0) == '~') {
            logger.trace("Expanding cache path: " + cachePath);

            expandedCachePath =
                System.getProperty("user.home") +
                cachePath.substring(1);

            logger.trace("Cache path expanded to: " + expandedCachePath);
        }
        else {
            expandedCachePath = cachePath;
        }

        this.cachePath = new File(expandedCachePath).getAbsoluteFile();
        logger.debug("Cache path set to: " + expandedCachePath);

        final String projectIDHash = String.format("%08X.cache",
                projectID == null ? 0 : HashUtil.hashCode(projectID));

        this.file = new File(this.cachePath, projectIDHash);
        logger.debug("Cache file for this project is: " + this.file);
    }

    public byte[] get() {
        try {
            if (!file.isFile()) {
                logger.trace("Cache file does not exist: " + file);
                return null;
            }

            return FileUtils.readFileToByteArray(file);
        }
        catch (final IOException e) {
            logger.error("Could not read from cache: " + file + " (" + e.getMessage() + ")");
            return null;
        }
    }

    public void set(final byte[] body) {
        if (!cachePath.isDirectory()) {
            logger.trace("Cache path does not exist, creating: " + cachePath);
            cachePath.mkdirs();
        }

        try {
            FileUtils.writeByteArrayToFile(file, body);
            logger.debug("Wrote cache file: " + file);
        }
        catch (final IOException e) {
            logger.error("Could not save cache file: " + file + "(" + e.getMessage() + ")");
        }
    }

    public void delete() {
        file.delete();
    }
}
