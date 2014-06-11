package com.dslplatform.compiler.client.params;

import java.io.File;

public class MigrationFilePath implements Param {
    public final File migrationFilePath;

    public MigrationFilePath(
            final File migrationFilePath) {
        this.migrationFilePath = migrationFilePath;
    }

    @Override
    public boolean equals(final Object that) {
        if (!(that instanceof MigrationFilePath) || that == null) return false;

        final MigrationFilePath thatMigrationFilePath = (MigrationFilePath) that;
        return migrationFilePath.equals(thatMigrationFilePath.migrationFilePath);
    }

    @Override
    public int hashCode() {
        return migrationFilePath.hashCode();
    }

    @Override
    public String toString() {
        return "MigrationFilePath(" + migrationFilePath + ")";
    }
}
