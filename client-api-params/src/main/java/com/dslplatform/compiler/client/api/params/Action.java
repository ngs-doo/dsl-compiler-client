/**
 * Copyright (C) 2013 Nova Generacija Softvera d.o.o. (HR), <https://dsl-platform.com/>
 */
package com.dslplatform.compiler.client.api.params;

import java.util.Map;
import java.util.TreeMap;

public enum Action implements Param {
    CLEAN("clean"),
    CLONE("clone"),
    DIFF("diff"),
    PARSE("parse"),
    PARSE_AND_DIFF("parse and diff"),
    UPDATE("update"),
    UPDATE_UNSAFE("update", new KV("migration", "unsafe"));

    // -------------------------------------------------------------------------

    public final String action;
    public final Map<String, String> metadata;

    private Action(final String action, final KV ...params) {
        this.action = action;
        metadata = new TreeMap<String, String>();
        for( final KV param : params) {
          metadata.put(param.key, param.value);
        }
    }

    // -------------------------------------------------------------------------

    @Override
    public boolean allowMultiple() { return false; }

    @Override
    public void addToPayload(final XMLOut xO) {
        xO.start("action").node("name", action);
        if (!metadata.isEmpty()) {
            xO.start("metadata");
            for (final Map.Entry<String, String> entry : metadata.entrySet()) {
                xO.node(entry.getKey(), entry.getValue());
            }
            xO.end();
        }
        xO.end();
    }

    static final class KV {
        public final String key;
        public final String value;

        public KV(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }
}
