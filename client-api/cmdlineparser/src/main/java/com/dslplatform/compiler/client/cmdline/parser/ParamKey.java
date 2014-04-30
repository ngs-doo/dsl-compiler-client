package com.dslplatform.compiler.client.cmdline.parser;

public enum ParamKey {
    PROJECT_PROPERTIES_PATH_KEY("project-properties-path"),

    USERNAME_KEY("username"),
    PROJECT_ID_KEY("project-id"),
    PROJECT_NAME_KEY("project-name"),
    PACKAGE_NAME_KEY("package-name"),
    TARGET_KEY("target"),

    WITH_ACTIVE_RECORD_KEY("with-active-record"),
    WITH_JAVA_BEANS_KEY("with-java-beans"),
    WITH_JACKSON_KEY("with-jackson"),
    WITH_HELPER_METHODS_KEY("with-helper-methods");

    public final String paramKey;

    private ParamKey(
            final String paramKey) {
        this.paramKey = paramKey;
    }

    @Override
    public String toString() {
        return paramKey;
    }
}
