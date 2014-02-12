package com.dslplatform.compiler.client.cmdline.logger;

import com.dslplatform.compiler.client.io.Logger;

public class LoggerSLF4J implements Logger {
    private final org.slf4j.Logger logger;

    public LoggerSLF4J(
            final org.slf4j.Logger logger) {
        this.logger = logger;
    }

    @Override
    public void setLevel(final Level level) {
        // NOOP: Cannot set level of Log4J logger without knowing the underlying implementation
    }

    @Override
    public void trace(final String message) {
        logger.trace(message);
    }

    @Override
    public void debug(final String message) {
        logger.debug(message);
    }

    @Override
    public void info(final String message) {
        logger.info(message);
    }

    @Override
    public void warn(final String message) {
        logger.warn(message);
    }

    @Override
    public void error(final String message) {
        logger.error(message);
    }
}
