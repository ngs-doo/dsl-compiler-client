package com.dslplatform.compiler.client.diff;

public class MigrationStrip {
    private final static String droppedIndicator = "DROPED";
    private final static String migration_description_start = "/*MIGRATION_DESCRIPTION";
    private final static String migration_description_end = "MIGRATION_DESCRIPTION*/";

    public static boolean findDestructive(String migrationInformation) {
        return migrationInformation.contains(droppedIndicator); // todo - find keywords
    }
    public static String stripInformationComents(String migration) {
        int migrationInfoStart = migration.indexOf(migration_description_start) + migration_description_start.length();
        int migrationInfoEnd = migration.indexOf(migration_description_end);
        return (migrationInfoEnd > -1 && migrationInfoStart > -1) ? migration.substring(migrationInfoStart, migrationInfoEnd) : "";
    }
}
