package com.dslplatform.compiler.client.params;

import java.util.regex.Pattern;
/**
 * Actions supported by the command line client
 */
public enum Action {

    UPDATE("update")
    , CONFIG("config")
    , PARSE("parse")
    , GET_CHANGES("diff")
    , LAST_DSL("last-dsl")
    , GENERATE_SOURCES("generate-sources")
    , DOWNLOAD_GENERATED_MODEL("download-generated-model")
    , UNMANAGED_CS_SERVER("unmanaged-cs-server")// <- parametar je folder name
    , UNMANAGED_SOURCE("unmanaged-source")
    , UNMANAGED_SQL_MIGRATION("unmanaged-sql-migration") // unmanaged postgres migration
    , UPGRADE_UNMANAGED_DATABASE("upgrade-unmanaged-database")
    , DEPLOY_UNMANAGED_SERVER("deploy-unmanaged-server");
    // FW: UPGRADE_DATABASE

//  CREATE_PROJECT("")
//, DOWNLOAD_PROJECT("")

//, DOWNLOAD_TEMPLATE("")
//, LIST_TEMPLATES("")
//    GENERATE_MIGRATION_SQL("generate-migration-sql"),
//    GENERATE_UNMANAGED_SOURCES("generate-unmanaged-sources"),


    public final String actionKey;

    private final Pattern actionPattern;

    private Action(final String actionKey) {
        this.actionKey = actionKey;
        this.actionPattern = Pattern.compile("(?i)" + actionKey);
    }

    @Override
    public String toString() {
        return actionKey;
    }

    public static Action find(final String actionName) {
        for (final Action action : Action.values()) {
            if (action.actionPattern.matcher(actionName).matches()) return action;
        }
        return null;
    }

    public static String getValidActions() {
        final StringBuilder sb = new StringBuilder();
        for (final Action action : Action.values()) {
            sb.append(action).append(", ");
        }
        sb.setLength(sb.length() - 2);
        return sb.toString();
    }
}
