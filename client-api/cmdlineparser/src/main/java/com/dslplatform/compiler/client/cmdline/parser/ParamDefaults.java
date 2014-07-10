package com.dslplatform.compiler.client.cmdline.parser;

import com.dslplatform.compiler.client.params.Target;

public enum ParamDefaults {
    TARGET_DEFAULT(Target.JAVA_CLIENT),
    PACKAGE_NAME_DEFAULT("model"),
    GENERATED_MODEL_DEFAULT("generatedModel.dll"),
    REVENJ_VERSION_DEFAULT("1.0.1"),
    REVENJ_PATH_DEFAULT(System.getProperty("java.io.tmpdir") + "/" + "revenj"),

    WITH_ACTIVE_RECORD_DEFAULT(true),
    WITH_JAVA_BEANS_DEFAULT(false),
    WITH_JACKSON_DEFAULT(true),
    WITH_HELPER_METHODS_DEFAULT(true),

    SKIP_DIFF_DEFAULT(false),
    ALLOW_UNSAFE_DEFAULT(false),
    MANAGED_DEFAULT(false);

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
