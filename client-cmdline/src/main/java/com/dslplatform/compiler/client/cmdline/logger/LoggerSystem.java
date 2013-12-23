package com.dslplatform.compiler.client.cmdline.logger;

import com.dslplatform.compiler.client.io.Logger;

public class LoggerSystem implements Logger {
    private Level level;

    public LoggerSystem(
            final Level level) {
        this.level = level;
    }

    private void log(final Level level, final String message) {
        if (this.level.compareTo(level) >= 0) {
            System.err.println("[" + level + "] " + message);
        }
    }

    @Override
    public void setLevel(final Level level) {
        this.level = level;
    }

    @Override
    public void trace(final String message) {
        log(Level.TRACE, message);
    }

    @Override
    public void debug(final String message) {
        log(Level.DEBUG, message);
    }

    @Override
    public void info(final String message) {
        log(Level.INFO, message);
    }

    @Override
    public void warn(final String message) {
        log(Level.WARN, message);
    }

    @Override
    public void error(final String message) {
        log(Level.ERROR, message);
    }
}
