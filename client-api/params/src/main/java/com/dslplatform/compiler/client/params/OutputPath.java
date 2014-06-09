package com.dslplatform.compiler.client.params;

import java.io.File;

public class OutputPath implements Param {
    public final File outputPath;

    public OutputPath(
            final File outputPath) {
        this.outputPath = outputPath;
    }

    @Override
    public boolean equals(final Object that) {
        if (!(that instanceof OutputPath) || that == null) return false;

        final OutputPath thatOutputPath = (OutputPath) that;
        return outputPath.equals(thatOutputPath.outputPath);
    }

    @Override
    public int hashCode() {
        return outputPath.hashCode();
    }

    @Override
    public String toString() {
        return "OutputPath(" + outputPath + ")";
    }
}
