package com.dslplatform.compiler.client.api.logging;

public class LoggerSystemOut implements Logger {
    private final Level level;

    public LoggerSystemOut(final Level level) {
        this.level = level;
    }

    private void log(final Level level, final String message) {
        if (this.level != null && level.compareTo(this.level) >= 0) {
            System.out.println("[" + level + "] " + message);
        }
    }

    @Override
    public void trace(final String message) {
        log(Level.TRACE, message);
    }

    @Override
    public void debug(final String message) {
        log(Level.TRACE, message);
    }

    @Override
    public void info(final String message) {
        log(Level.TRACE, message);
    }

    @Override
    public void warn(final String message) {
        log(Level.TRACE, message);
    }

    @Override
    public void error(final String message) {
        log(Level.TRACE, message);
    }
}
