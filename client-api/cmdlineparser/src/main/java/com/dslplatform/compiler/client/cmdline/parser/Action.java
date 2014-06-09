package com.dslplatform.compiler.client.cmdline.parser;

/**
 * Actions supported by the command line client
 */
public enum Action {

    GENERATE_MIGRATION_SQL("generate-migration-sql"),
    GENERATE_UNMANAGED_SOURCES("generate-unmanaged-sources"),
    PARSE("parse");

    public final String actionKey;

    private Action(
            final String actionKey) {
        this.actionKey = actionKey;
    }

    @Override
    public String toString() {
        return actionKey;
    }

}
