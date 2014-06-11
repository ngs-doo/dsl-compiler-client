package com.dslplatform.compiler.client.params;



/**
 * Actions supported by the command line client
 */
public enum Action {

    UPDATE("update")
    , GET_CHANGES("diff")
    , LAST_DSL("last-dsl")
    , CONFIG("config")
    , PARSE("parse")
    , GENERATE_SOURCES("generate-sources")
    , UNMANAGED_CS_SERVER("unmanaged-cs-server")// <- parametar je folder name
    , UNMANAGED_SOURCE("unmanaged-source")
    , UNMANAGED_SQL_MIGRATION("unmanaged-sql-migration") // unmanaged postgres migration
    , UPGRADE_UNMANAGED_DATABASE("upgrade-unmanaged-database")
    , DEPLOY_UNMANAGED_SERVER("deploy_unmanaged_server");
    // FW: UPGRADE_DATABASE

//  CREATE_PROJECT("")
//, DOWNLOAD_PROJECT("")
//, DOWNLOAD_GENERATED_MODEL("")
//, DOWNLOAD_TEMPLATE("")
//, LIST_TEMPLATES("")
//    GENERATE_MIGRATION_SQL("generate-migration-sql"),
//    GENERATE_UNMANAGED_SOURCES("generate-unmanaged-sources"),


    public final String actionKey;


    private Action(
            final String actionKey) {
        this.actionKey = actionKey;
    }

    @Override
    public String toString() {
        return actionKey;
    }

    public static Action find(final String action) {

        try{
            final Action a = Action.valueOf(action);
            return a;
        }catch(final IllegalArgumentException e){
            return null;
        }
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
