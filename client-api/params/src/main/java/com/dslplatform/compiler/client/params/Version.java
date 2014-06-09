package com.dslplatform.compiler.client.params;

import java.util.regex.Pattern;

public enum Version {
    CLIENT(true),
    PORTABLE(false),
    SERVER(false);

    public final String versionName;
    final Pattern versionPattern;

    // use case versions are optional
    private Version(final boolean isUseCase) {
        final String lowerName = name().toLowerCase();
        versionName = isUseCase ? "" : '_' + lowerName;

        final String separatedPattern = "[-_ ]?" + lowerName;
        versionPattern = Pattern.compile(isUseCase
                ? "(?:" + separatedPattern + ")?"
                : separatedPattern);
    }

    @Override
    public String toString() {
        return versionName;
    }
}
