package com.dslplatform.compiler.client.params;

import java.io.File;

public class DSLPath implements Param {
    public final File dslPath;

    public DSLPath(
            final File dslPath) {
        this.dslPath = dslPath;
    }

    @Override
    public boolean equals(final Object that) {
        if (!(that instanceof DSLPath) || that == null) return false;

        final DSLPath thatDSLPath = (DSLPath) that;
        return dslPath.equals(thatDSLPath.dslPath);
    }

    @Override
    public int hashCode() {
        return dslPath.hashCode();
    }

    @Override
    public String toString() {
        return "DSLPath(" + dslPath + ")";
    }
}
