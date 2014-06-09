package com.dslplatform.compiler.client.params;

import java.io.File;

public class CachePath implements Param {
    public final File cachePath;

    public CachePath(
            final File cachePath) {
        this.cachePath = cachePath;
    }

    @Override
    public boolean equals(final Object that) {
        if (!(that instanceof CachePath) || that == null) return false;

        final CachePath thatCachePath = (CachePath) that;
        return cachePath.equals(thatCachePath.cachePath);
    }

    @Override
    public int hashCode() {
        return cachePath.hashCode();
    }

    @Override
    public String toString() {
        return "CachePath(" + cachePath + ")";
    }
}
