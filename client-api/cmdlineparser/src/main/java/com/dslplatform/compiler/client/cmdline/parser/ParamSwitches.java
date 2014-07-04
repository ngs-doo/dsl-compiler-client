package com.dslplatform.compiler.client.cmdline.parser;

import static com.dslplatform.compiler.client.cmdline.parser.ParamKey.*;

public enum ParamSwitches {
    END_OF_PARAMS("--"),
    HELP("-h", "--help"),

    /* Read properties from file */
    PROJECT_PROPERTIES_PATH_SWITCHES(PROJECT_PROPERTIES_PATH_KEY, "-f", "--" + PROJECT_PROPERTIES_PATH_KEY),

    DB_USERNAME_SWITCHES(DB_USERNAME_KEY),
    DB_PASSWORD_SWITCHES(DB_PASSWORD_KEY),
    DB_HOST_SWITCHES(DB_HOST_KEY),
    DB_PORT_SWITCHES(DB_PORT_KEY),
    DB_DATABASE_NAME_SWITCHES(DB_DATABASE_NAME_KEY),
    DB_CONNECTION_STRING_SWITCHES(DB_CONNECTION_STRING_KEY),

    MIGRATION_FILE_PATH_SWITCHES(MIGRATION_FILE_PATH_KEY),
    REVENJ_PATH_SWITCHES(REVENJ_PATH_KEY),

    COMPILATION_TARGET_PATH_SWITCH(COMPILATION_TARGET_KEY),
    MONO_APPLICATION_PATH_SWITCH(MONO_APPLICATION_KEY),

    /* Single property value */
    USERNAME_SWITCHES(USERNAME_KEY, "-u", "--" + USERNAME_KEY),
    PASSWORD_SWITCHES(PASSWORD_KEY, "-u", "--" + PASSWORD_KEY),
    PROJECT_ID_SWITCHES(PROJECT_ID_KEY, "-i", "--" + PROJECT_ID_KEY),
    PROJECT_NAME_SWITCHES(PROJECT_NAME_KEY, "-k", "--" + PROJECT_NAME_KEY),
    PACKAGE_NAME_SWITCHES(PACKAGE_NAME_KEY, "-n", "--" + PACKAGE_NAME_KEY),
    TARGET_SWITCHES(TARGET_KEY, "-t", "--" + TARGET_KEY),
    OUTPUT_PATH_SWITCHES(OUTPUT_PATH_KEY, "-o", "--" + OUTPUT_PATH_KEY),
    DSL_PATH_SWITCHES(DSL_PATH_KEY),
    CACHE_PATH_SWITCHES(CACHE_PATH_KEY),
    LOGGING_LEVEL_SWITCHES(LOGGING_LEVEL_KEY, "-l", "--" + LOGGING_LEVEL_KEY),

    /* Flags */
    WITH_ACTIVE_RECORD_SWITCHES(WITH_ACTIVE_RECORD_KEY),
    WITH_JAVA_BEANS_SWITCHES(WITH_JAVA_BEANS_KEY),
    WITH_JACKSON_SWITCHES(WITH_JACKSON_KEY),
    WITH_HELPER_METHODS_SWITCHES(WITH_HELPER_METHODS_KEY),

    SKIP_DIFF_SWITCHES(SKIP_DIFF_KEY),
    ALLOW_UNSAFE_SWITCHES(ALLOW_UNSAFE_KEY),
    MANAGED_SWITCH(MANAGED_KEY);

    private final ParamKey paramKey;
    private final String[] switches;

    private ParamSwitches(final ParamKey paramKey) {
        this.paramKey = paramKey;
        this.switches = new String[]{"--" + paramKey};
    }

    private ParamSwitches(final ParamKey paramKey,
                          final String... switches) {
        this.paramKey = paramKey;
        this.switches = switches;
    }

    private ParamSwitches(final String... switches) {
        this.paramKey = null;
        this.switches = switches;
    }

    public class SwitchArgument {
        public final boolean isSwitch;
        public final boolean isShortSwitch;

        public boolean isEqual() {
            return argBody != null && argBody.isEmpty();
        }

        public boolean hasBody() {
            return argBody != null && !argBody.isEmpty();
        }

        private final String sw;

        public final String getSwitch() {
            if (sw == null) throw new IllegalArgumentException("Argument is not a switch!");
            return sw;
        }

        private final String argBody;

        public final String getArgument() {
            if (argBody == null)
                throw new IllegalArgumentException("There is no argument body; argument is not a switch!");

            if (isShortSwitch) return argBody;

            if (hasBody()) {
                if (argBody.charAt(0) != '=') throw new IllegalArgumentException(
                        "Long switch argument [" + sw + argBody + "] needs to have a '=' separator!");
                return argBody.substring(1);
            }

            return argBody;
        }

        private SwitchArgument(final String arg) {
            String sw = null;
            String argBody = null;

            for (final String aSwitch : switches) {
                if (arg.startsWith(aSwitch)) {
                    sw = aSwitch;
                    argBody = arg.substring(sw.length());
                    break;
                }
            }

            isSwitch = sw != null;
            isShortSwitch = isSwitch && sw.matches("^-[^-]");

            this.sw = sw;
            this.argBody = argBody;
        }
    }

    public SwitchArgument examine(final String arg) {
        return new SwitchArgument(arg);
    }

    public boolean is(final String arg) {
        return examine(arg).isEqual();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (final String sw : switches) sb.append(sw).append(", ");
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    public String[] getSwitches() {
        return switches;
    }

    public ParamKey getParamKey() {
        return paramKey;
    }

}
