package com.dslplatform.compiler.client.cmdline.parser;

public enum ParamKey {
    PROJECT_PROPS_PATH_KEY("project-properties-path"),

    USERNAME_KEY("username"),
    PROJECT_ID_KEY("project-id"),
    PROJECT_NAME_KEY("project-name"),
    PACKAGE_NAME_KEY("package-name"),
    TARGET_KEY("target"),
    OUTPUT_PATH_KEY("output-path"),
    CACHE_PATH_KEY("cache-path"),
    LOGGING_LEVEL_KEY("logging-level"),

    WITH_ACTIVE_RECORD_KEY("with-active-record"),
    WITH_JAVA_BEANS_KEY("with-java-beans"),
    WITH_JACKSON_KEY("with-jackson"),
    WITH_HELPER_METHODS_KEY("with-helper-methods"),

    SKIP_DIFF_KEY("skip-diff"),
    ALLOW_UNSAFE_KEY("allow-unsafe"),

    /* Action keys */
    GENERATE_MIGRATION_SQL_KEY("generate-migration-sql"),
    GENERATE_UNMANAGED_SOURCES_KEY("generate-unmanaged-sources"),
    PARSE_KEY("parse");

    // TODO:
//  skipdiff
//  confirm unsafe
//  cache path
    /*
     * Email <- value obj
     *
     * Database Connection <- value obj
     *  - connection str
     *  - username
     *  - passwd
     *
     * server name
     * app name
     *
     * */

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
