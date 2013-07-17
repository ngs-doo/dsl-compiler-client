/**
 * Copyright (C) 2013 Nova Generacija Softvera d.o.o. (HR), <https://dsl-platform.com/>
 */
package com.dslplatform.compiler.client.api.params;

public class PackageName implements Param {
    public final String packageName;

    public PackageName(final String packageName) {
        this.packageName = packageName;
    }

    // -------------------------------------------------------------------------

    @Override
    public boolean allowMultiple() { return false; }

    @Override
    public void addToPayload(final XMLOut xO) {
        if (packageName != null && !packageName.isEmpty())
            xO.node("package-name", packageName);
    }
}
