package com.dslplatform.compiler.client.api.params;

public class Target implements Param {
    public final String version;
    public final String branch;

    public Target(final String version, final String branch) {
        this.version = version;
        this.branch = branch;
    }

    public Target(final String version) {
        this(version, "stable");
    }

    // -------------------------------------------------------------------------

    @Override
    public boolean allowMultiple() { return false; }

    @Override
    public void addToPayload(final XMLOut xO) {
        xO.start("target")
            .node("version", version)
            .node("branch", branch)
        .end();
    }
}
