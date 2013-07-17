package com.dslplatform.compiler.client.api.logging;

public interface Logger {
    public static enum Level {
        ALL, TRACE, DEBUG, INFO, WARN, ERROR, NONE;
    }

    public void trace(final String message);
    public void debug(final String message);
    public void info(final String message);
    public void warn(final String message);
    public void error(final String message);
}
