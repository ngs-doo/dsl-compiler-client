package com.dslplatform.compiler.client.cmdline.parser;

import com.dslplatform.compiler.client.params.Target;

public enum ParamDefaults {
    TARGET_DEFAULT(Target.JAVA_CLIENT),
    PACKAGE_NAME_DEFAULT("model"),

    WITH_ACTIVE_RECORD_DEFAULT(true),
    WITH_JAVA_BEANS_DEFAULT(false),
    WITH_JACKSON_DEFAULT(true),
    WITH_HELPER_METHODS_DEFAULT(true),

    SKIP_DIFF_DEFAULT(false),
    ALLOW_UNSAFE_DEFAULT(true);

    public final String defaultValue;

    private ParamDefaults(
            final Object defaultValue) {
        this.defaultValue = String.valueOf(defaultValue);
    }

    @Override
    public String toString() {
        return defaultValue;
    }
}
