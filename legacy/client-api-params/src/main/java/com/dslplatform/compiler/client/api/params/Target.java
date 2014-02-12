package com.dslplatform.compiler.client.api.params;

public class Target implements Param {
    public final String version;
    public final String branch;

    public Target(
            final String branch,
            final String version) {
        this.branch = branch;
        this.version = version;
    }

    // -------------------------------------------------------------------------

    @Override
    public boolean allowMultiple() {
        return false;
    }

    // format: OFF
    @Override
    public void addToPayload(final XMLOut xO) {
        xO.start("target")
            .node("branch", branch)
            .node("version", version)
        .end();
    }
}
