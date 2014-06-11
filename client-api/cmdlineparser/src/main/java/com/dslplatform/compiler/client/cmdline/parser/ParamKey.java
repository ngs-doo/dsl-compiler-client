package com.dslplatform.compiler.client.cmdline.parser;

public enum ParamKey {
    PROJECT_PROPERTIES_PATH_KEY("project-properties-path"),

    /* Used only to enumerate actions in the properties read */
    ACTIONS_KEY("actions"), /// XXX: Mind that this can probably be read from the properties file

    /* Database authentication properties, these properties will all be aggregated in a DBAuth object */
    DB_USERNAME_KEY("db-username"),
    DB_PASSWORD_KEY("db-password"),
    DB_HOST_KEY("db-host"),
    DB_PORT_KEY("db-port"),
    DB_DATABASE_NAME_KEY("db-database-name"),
    DB_CONNECTION_STRING_KEY("db-connection-string"),

    MIGRATION_FILE_PATH_KEY("migration-file"),

    USERNAME_KEY("username"),
    PROJECT_ID_KEY("project-id"),
    PROJECT_NAME_KEY("project-name"),
    PACKAGE_NAME_KEY("package-name"),
    PASSWORD_KEY("password"),
    TARGET_KEY("target"),
    OUTPUT_PATH_KEY("output-path"),
    DSL_PATH_KEY("dsl-path"),
    CACHE_PATH_KEY("cache-path"),
    LOGGING_LEVEL_KEY("logging-level"),

    WITH_ACTIVE_RECORD_KEY("with-active-record"),
    WITH_JAVA_BEANS_KEY("with-java-beans"),
    WITH_JACKSON_KEY("with-jackson"),
    WITH_HELPER_METHODS_KEY("with-helper-methods"),

    SKIP_DIFF_KEY("skip-diff"),
    ALLOW_UNSAFE_KEY("allow-unsafe");

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
