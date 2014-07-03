package com.dslplatform.compiler.client.params;

import java.io.File;

public class MonoApplicationPath implements Param {
    public final File monoApplicationPath;

    public MonoApplicationPath(File monoApplicationPath) {
        this.monoApplicationPath = monoApplicationPath;
    }

    @Override
    public boolean equals(final Object that) {
        if (!(that instanceof MonoApplicationPath) || that == null) return false;

        final MonoApplicationPath thatMonoApplicationPath = (MonoApplicationPath) that;
        return monoApplicationPath.equals(thatMonoApplicationPath.monoApplicationPath);
    }

    @Override
    public int hashCode() {
        return monoApplicationPath.hashCode();
    }

    @Override
    public String toString() {
        return "MonoApplicationPath(" + monoApplicationPath + ")";
    }
}
