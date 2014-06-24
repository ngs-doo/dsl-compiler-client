package com.dslplatform.compiler.client.params;

import static com.dslplatform.compiler.client.params.Language.*;
import static com.dslplatform.compiler.client.params.Version.*;

import java.util.regex.Pattern;

public enum Target {
    CSHARP_CLIENT(CSHARP, CLIENT),
    CSHARP_PORTABLE(CSHARP, PORTABLE),
    CSHARP_SERVER(CSHARP, SERVER, "CSharpServer"),
    JAVA_CLIENT(JAVA, CLIENT, "Java"),
    PHP_CLIENT(PHP, CLIENT),
    SCALA_CLIENT(SCALA, CLIENT, "Scala"),
    SCALA_SERVER(SCALA, SERVER, "ScalaServer");

    public final Language language;
    public final Version version;
    public final String targetName;
    final Pattern targetPattern;

    private Target(final Language language, final Version version) {
        this.language = language;
        this.version = version;
        targetName = language.languageName + version.versionName;
        targetPattern = Pattern.compile("(?i)" + language.languagePattern + version.versionPattern);
    }

    private Target(final Language language, final Version version, final String targetName) {
        this.language = language;
        this.version = version;
        this.targetName = targetName;
        targetPattern = Pattern.compile("(?i)" + language.languagePattern + version.versionPattern);
    }

    public static Target find(final String language) {
        for (final Target target : Target.values()) {
            if (target.targetPattern.matcher(language).matches()) return target;
        }
        return null;
    }

    public static String getValidTargets() {
        final StringBuilder sb = new StringBuilder();
        for (final Target target : Target.values()) {
            sb.append(target).append(", ");
        }
        sb.setLength(sb.length() - 2);
        return sb.toString();
    }

    @Override
    public String toString() {
        return targetName;
    }
}
