package com.dslplatform.compiler.client.api.params;

public class ProjectName implements Param {
    public final String projectName;

    public ProjectName(
            final String projectName) {
        this.projectName = projectName;
    }

    // -------------------------------------------------------------------------

    @Override
    public boolean allowMultiple() {
        return false;
    }

    // format: OFF
    @Override
    public void addToPayload(
            final XMLOut xO) {
        if (projectName != null && !projectName.isEmpty()) {
            xO.node("project-name", projectName);
        }
    }
}
