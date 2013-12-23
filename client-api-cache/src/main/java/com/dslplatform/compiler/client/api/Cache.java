package com.dslplatform.compiler.client.api;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import com.dslplatform.compiler.client.api.commons.HashUtil;
import com.dslplatform.compiler.client.api.commons.io.FileUtils;
import com.dslplatform.compiler.client.io.Logger;

public class Cache {
    private final Logger logger;

    private final File cachePath;
    private final File file;

    public Cache(
            final Logger logger,
            final File cachePath,
            final UUID projectID) {
        this.logger = logger;

        this.cachePath = cachePath;
        logger.debug("Cache path set to: " + cachePath);

        final String projectIDHash = String.format("%08X.cache",
                projectID == null ? 0 : HashUtil.hashCode(projectID));

        file = new File(this.cachePath, projectIDHash);
        logger.debug("Cache file for this project is: " + file);
    }

    public byte[] get() {
        try {
            if (!file.isFile()) {
                logger.trace("Cache file does not exist: " + file);
                return null;
            }

            return FileUtils.readFileToByteArray(file);
        } catch (final IOException e) {
            logger.error("Could not read from cache: " + file + " ("
                    + e.getMessage() + ")");
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
        } catch (final IOException e) {
            logger.error("Could not save cache file: " + file + "("
                    + e.getMessage() + ")");
        }
    }

    public void delete() {
        file.delete();
    }
}
