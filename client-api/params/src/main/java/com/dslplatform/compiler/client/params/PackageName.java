package com.dslplatform.compiler.client.params;

public class PackageName implements Param {
    public final String packageName;

    public PackageName(
            final String packageName) {
        this.packageName = packageName;
    }

    @Override
    public boolean equals(final Object that) {
        if (!(that instanceof PackageName) || that == null) return false;

        final PackageName thatPackageName = (PackageName) that;
        return packageName.equals(thatPackageName.packageName);
    }

    @Override
    public int hashCode() {
        return packageName.hashCode();
    }

    @Override
    public String toString() {
        return "PackageName(" + packageName + ")";
    }
}
