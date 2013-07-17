/**
 * Copyright (C) 2013 Nova Generacija Softvera d.o.o. (HR), <https://dsl-platform.com/>
 */
package com.dslplatform.compiler.client.api.params;

import java.util.LinkedHashMap;
import java.util.Map;

public class DSL implements Param {
    public final Map<String, String> files;

    public DSL(final Map<String, String> files) {
        this.files = new LinkedHashMap<String, String>(files);
    }

    // -------------------------------------------------------------------------

    @Override
    public boolean allowMultiple() { return true; }

    @Override
    public void addToPayload(final XMLOut xO) {
        xO.start("dsl");
        for (final Map.Entry<String, String> file : files.entrySet()) {
            xO.start("file")
                .node("filename", file.getKey())
                .node("body", file.getValue())
            .end();
        }
        xO.end();
    }
}
