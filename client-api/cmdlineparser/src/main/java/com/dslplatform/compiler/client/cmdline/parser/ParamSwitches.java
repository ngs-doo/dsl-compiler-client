package com.dslplatform.compiler.client.cmdline.parser;

import static com.dslplatform.compiler.client.cmdline.parser.ParamKey.*;

public enum ParamSwitches {
    END_OF_PARAMS("--"),
    HELP("-h", "--help"),

    PROJECT_PROPERTIES_PATH_SWITCHES("-f", "--" + PROJECT_PROPERTIES_PATH_KEY),

    USERNAME_SWITCHES("-u", "--" + USERNAME_KEY),
    PROJECT_ID_SWITCHES("-i", "--" + PROJECT_ID_KEY),
    PROJECT_NAME_SWITCHES("-k", "--" + PROJECT_NAME_KEY),
    PACKAGE_NAME_SWITCHES("-n", "--" + PACKAGE_NAME_KEY),
    TARGET_SWITCHES("-t", "--" + TARGET_KEY),

    WITH_ACTIVE_RECORD_SWITCHES ("--" + WITH_ACTIVE_RECORD_KEY),
    WITH_JAVA_BEANS_SWITCHES    ("--" + WITH_JAVA_BEANS_KEY),
    WITH_JACKSON_SWITCHES       ("--" + WITH_JACKSON_KEY),
    WITH_HELPER_METHODS_SWITCHES("--" + WITH_HELPER_METHODS_KEY);

    private final String[] switches;

    private ParamSwitches(
            final String... switches) {
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
            if (argBody == null) throw new IllegalArgumentException("There is no argument body; argument is not a switch!");

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

            for (final String cur : switches) {
                if (arg.startsWith(cur)) {
                    sw = cur;
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
        for(final String sw : switches) sb.append(sw).append(", ");
        sb.setLength(sb.length() -1);
        return sb.toString();
    }
}
