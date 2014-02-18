package com.dslplatform.compiler.client.api.model.compiler;

import com.fasterxml.jackson.annotation.*;

public final class DSL implements java.io.Serializable {
    public DSL(
            final java.util.Map<String, String> dslFiles) {
        setDslFiles(dslFiles);
    }

    public DSL() {
        this.dslFiles = new java.util.HashMap<String, String>();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + 1690816091;
        result = prime * result
                + (this.dslFiles != null ? this.dslFiles.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;

        if (!(obj instanceof DSL)) return false;
        final DSL other = (DSL) obj;

        if (!(this.dslFiles != null && this.dslFiles.equals(other.dslFiles) || this.dslFiles == null
                && other.dslFiles == null)) return false;

        return true;
    }

    @Override
    public String toString() {
        return "DSL(" + dslFiles + ')';
    }

    private static final long serialVersionUID = 0x0097000a;

    private java.util.Map<String, String> dslFiles;

    @JsonProperty("dslFiles")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public java.util.Map<String, String> getDslFiles() {
        return dslFiles;
    }

    public DSL setDslFiles(final java.util.Map<String, String> value) {
        if (value == null)
            throw new IllegalArgumentException(
                    "Property \"dslFiles\" cannot be null!");
        this.dslFiles = value;

        return this;
    }
}
