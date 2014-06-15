package com.dslplatform.compiler.client.params;

import java.util.HashMap;
import java.util.Map;

public class DSL {
    public final Map<String, String> files;

    public DSL(final Map<String, String> files) {
        this.files = files;
    }

    public DSL() { files = new HashMap<String, String>(); }

    public static DSL empty() {
        return new DSL();
    }
}
