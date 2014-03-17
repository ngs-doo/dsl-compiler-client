package com.dslplatform.compiler.client.api.core;

import java.util.Map;

public class Migration {
    public final int ordinal;
    public final String version;
    public final Map<String, String> dsls;

    public Migration(
            final int ordinal,
            final String version,
            final Map<String, String> dsls) {
        this.ordinal = ordinal;
        this.version = version;
        this.dsls = dsls;
    }
}
