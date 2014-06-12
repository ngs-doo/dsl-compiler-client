package com.dslplatform.compiler.client.params;

import java.io.File;

public class RevenjPath implements Param {
    public final File revenjPath;

    public RevenjPath(
            final File revenjPath) {
        this.revenjPath = revenjPath;
    }

    @Override
    public boolean equals(final Object that) {
        if (!(that instanceof RevenjPath) || that == null) return false;

        final RevenjPath thatRevenjPath = (RevenjPath) that;
        return revenjPath.equals(thatRevenjPath.revenjPath);
    }

    @Override
    public int hashCode() {
        return revenjPath.hashCode();
    }

    @Override
    public String toString() {
        return "RevenjPath(" + revenjPath + ")";
    }
}
