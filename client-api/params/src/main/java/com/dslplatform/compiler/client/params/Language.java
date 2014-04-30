package com.dslplatform.compiler.client.params;

import java.util.regex.Pattern;

public enum Language {
    CSHARP("c#"),
    JAVA,
    PHP,
    SCALA;

    public final String languageName;
    final Pattern languagePattern;

    private Language(final String... aliases) {
        languageName = name().toLowerCase();
        if (aliases.length == 0) {
            this.languagePattern = Pattern.compile(languageName);
        }
        else {
            final StringBuilder sb = new StringBuilder("(?:").append(languageName);
            for (final String alias : aliases) sb.append('|').append(Pattern.quote(alias));
            this.languagePattern = Pattern.compile(sb.append(')').toString());
        }

    }

    @Override
    public String toString() {
        return languageName;
    }
}
