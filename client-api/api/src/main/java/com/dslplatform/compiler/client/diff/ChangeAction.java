package com.dslplatform.compiler.client.diff;

public enum ChangeAction {
    SKIPPED("skipping"),
    CREATED_DIR("creating directory"),
    CREATED("creating"),
    NO_CHANGE("up to date, skipping"),
    MODIFIED("modifying"),
    MOVED("moving"),
    COPY("copying"),
    DELETED_DIR("deleting directory"),
    DELETED("deleting");

    private String description;

    private ChangeAction(
            final String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return description;
    }
}
