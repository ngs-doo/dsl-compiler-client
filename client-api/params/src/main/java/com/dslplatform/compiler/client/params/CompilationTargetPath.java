package com.dslplatform.compiler.client.params;

import java.io.File;

public class CompilationTargetPath implements Param {
    public final File compilationTargetPath;

    public CompilationTargetPath(File compilationTargetPath) {
        this.compilationTargetPath = compilationTargetPath;
    }

    @Override
    public boolean equals(final Object that) {
        if (!(that instanceof CompilationTargetPath) || that == null) return false;

        final CompilationTargetPath thatCompilationTargetPath = (CompilationTargetPath) that;
        return compilationTargetPath.equals(thatCompilationTargetPath.compilationTargetPath);
    }

    @Override
    public int hashCode() {
        return compilationTargetPath.hashCode();
    }

    @Override
    public String toString() {
        return "CompilationTargetPath(" + compilationTargetPath + ")";
    }
}
