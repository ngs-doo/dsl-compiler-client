package com.dslplatform.compiler.client.api.model.compiler;

import com.fasterxml.jackson.annotation.*;

public final class PackageName implements java.io.Serializable {
    public PackageName(
            final String packageName) {
        setPackageName(packageName);
    }

    public PackageName() {
        this.packageName = "";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + 993550195;
        result = prime * result
                + (this.packageName != null ? this.packageName.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;

        if (!(obj instanceof PackageName)) return false;
        final PackageName other = (PackageName) obj;

        if (!(this.packageName.equals(other.packageName))) return false;

        return true;
    }

    @Override
    public String toString() {
        return "PackageName(" + packageName + ')';
    }

    private static final long serialVersionUID = 0x0097000a;

    private String packageName;

    @JsonProperty("packageName")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String getPackageName() {
        return packageName;
    }

    public PackageName setPackageName(final String value) {
        if (value == null)
            throw new IllegalArgumentException(
                    "Property \"packageName\" cannot be null!");
        this.packageName = value;

        return this;
    }
}
