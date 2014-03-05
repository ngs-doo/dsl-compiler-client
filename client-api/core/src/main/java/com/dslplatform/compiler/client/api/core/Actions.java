package com.dslplatform.compiler.client.api.core;

import com.dslplatform.compiler.client.api.model.Client.DatabaseConnection;
import com.dslplatform.patterns.ServiceLocator;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

public interface Actions {

    public void parseDsl(final byte[] token,final String dsl);

    /**
     * Registers user with his email and informs of received email.
     */
    public void registerUser(final String email) throws IOException;

    /**
     * Creates test project with given nick and returns projectId
     */
    public void createTestProject(
            final byte[] token, final String projectNick);

    /**
     * Creates external project and returns id.
      */
    public void createExternalProject(
            final byte[] token,
            final String projectName,
            final String serverName,
            final String applicationName,
            final DatabaseConnection databaseConnection
    );

    public void createUnmanagedProject(
            final byte[] token,
            final String serverName,
            final String applicationName,
            final DatabaseConnection databaseConnection
    );

    /* TODO - unmanaged
       DownloadServerBinaries;       // (final byte[] token,UE) DownloadProject
       DownloadServerGeneratedModel; // (final byte[] token,UE) DownloadGeneratedModel
     */

    /**
     * Downloads binaries for a project.
     */
    public void downloadBinaries(final byte[] token,final UUID projectID);

    /**
     * Downloads Generated Model for a project.
     */
    public void downloadGeneratedModel(final byte[] token,final UUID projectID);

    /**
     * get dsl from ngs shema, send with new dsl to compare changes.
     *
     * @param databaseConnection
     * @param newdsl
     * @param version
     */
    public void inspectUnmanagedProjectChanges(final byte[] token,
            final DatabaseConnection databaseConnection,
            final String newdsl,
            final String version
    );

    /**
     * Get changes
     *
     * @param projectID
     * @param newdsl
     */
    public void inspectProjectChanges(final byte[] token,
            final UUID projectID,
            final String newdsl
    );

    public void getLastManagedDSL(final byte[] token,final UUID projectID);

    public void getLastUnManagedDSL(final byte[] token,final DatabaseConnection databaseConnection);

    public void getConfig(final byte[] token,
            final UUID projectID,
            final Set<String> targets,
            final String ns,
            final Set<String> options);

    public void diffWithLastDslUnmanaged(final byte[] token,final DatabaseConnection databaseConnection);

    public void diffWithLastDslManaged(final byte[] token, final UUID projectID);

    public void updateManagedProject(final byte[] token,
            final UUID projectID,
            final String namespace,
            final String migration,
            final String dsl,
            final Set<String> targets,
            final Set<String> options);

    public void updateUnManagedProject(final byte[] token,
            final DatabaseConnection databaseConnection,
            final String namespace,
            final String dsl,
            final Set<String> targets,
            final Set<String> options);

    public void generateMigrationSQL(final byte[] token,
            final String oldDsl,
            final String newDsl,
            final String version);

    public void generateSources(
            final byte[] token,
            final UUID projectID,
            final String namespace,
            final Set<String> targets,
            final Set<String> options
    );

    public void generateUnmanagedSources(
            final byte[] token,
            final String namespace,
            final Set<String> targets,
            final Set<String> options,
            final String dsl
    );

    public void getProjectByName(final byte[] token, final String projectName);
    public void getAllProjects(final byte[] token);
    public void renameProject(final byte[] token, final String oldName, final String newName);
    public void cleanProject(final byte[] token);
    public void templateGet(final byte[] token, final String templateName);
    public void templateCreate(final byte[] token, final String templateName);
    public void templateListAll(final byte[] token, final String templateName);
    public void templateDelete(final byte[] token, final String templateName);
}
