package com.dslplatform.compiler.client.io;

public interface Logger {
    public static enum Level {
        OFF,
        ERROR,
        WARN,
        INFO,
        DEBUG,
        TRACE,
        ALL;
    }

    public void setLevel(final Level level);

    public void trace(final String message);

    public void debug(final String message);

    public void info(final String message);

    public void warn(final String message);

    public void error(final String message);
}
