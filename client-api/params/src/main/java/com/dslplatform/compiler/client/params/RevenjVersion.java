package com.dslplatform.compiler.client.params;

public class RevenjVersion implements Param {
    public final String version;

    public RevenjVersion(String version) {
        this.version = version;
    }

    @Override
    public boolean equals(final Object that) {
        if (!(that instanceof RevenjVersion) || that == null) return false;

        final RevenjVersion thatProjectName = (RevenjVersion) that;
        return version.equals(thatProjectName.version);
    }

    @Override
    public int hashCode() {
        return version.hashCode();
    }

    @Override
    public String toString() {
        return "MonoVersion(" + version + ")";
    }
}
