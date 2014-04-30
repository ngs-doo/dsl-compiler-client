package com.dslplatform.compiler.client.params;

public class ProjectName implements Param {
    public final String projectName;

    public ProjectName(
            final String projectName) {
        if (projectName.matches(".*?\\s.*")) throw new IllegalArgumentException("ProjectName cannot contain whitespaces!");
        this.projectName = projectName;
    }

    @Override
    public boolean equals(final Object that) {
        if (!(that instanceof ProjectName) || that == null) return false;

        final ProjectName thatProjectName = (ProjectName) that;
        return projectName.equals(thatProjectName.projectName);
    }

    @Override
    public int hashCode() {
        return projectName.hashCode();
    }

    @Override
    public String toString() {
        return "ProjectName(" + projectName + ")";
    }
}
