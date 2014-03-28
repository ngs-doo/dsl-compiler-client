package com.dslplatform.compiler.client;

import com.dslplatform.compiler.client.api.core.*;
import com.dslplatform.compiler.client.processor.*;
import com.dslplatform.compiler.client.response.*;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ApiImpl implements Api {
    private final HttpRequestBuilder httpRequestBuilder;
    private final HttpTransport httpTransport;
    private final UnmanagedDSL unmanagedDSL;

    public ApiImpl(
            HttpRequestBuilder httpRequestBuilder,
            HttpTransport httpTransport, UnmanagedDSL unmanagedDSL) {
        this.httpRequestBuilder = httpRequestBuilder;
        this.httpTransport = httpTransport;
        this.unmanagedDSL = unmanagedDSL;
    }

    @Override public void registerUser(
            String email) {
    }

    @Override public ParseDSLResponse parseDsl(
            String token,
            Map<String, String> dsl
    ) {
        final HttpResponse httpResponse;
        try {
            httpResponse = httpTransport.sendRequest(httpRequestBuilder.parseDsl(token, dsl));
        } catch (IOException e) {
            return new ParseDSLResponse(false, e.getMessage(), false, null);
        }

        return new ParseDSLResponseProcessor().process(httpResponse);
    }

    @Override public void createTestProject(
            String token, String projectName) {

    }

    @Override public void createExternalProject(
            String token,
            String projectName,
            String serverName,
            String applicationName,
            Map<String, Object> databaseConnection) {

    }

    @Override public void downloadBinaries(
            String token, UUID projectID) {

    }

    @Override public void downloadGeneratedModel(
            String token, UUID projectID) {

    }

    @Override public void inspectManagedProjectChanges(
            String token,
            UUID projectID,
            Map<String, String> dsl) {

    }

    @Override public void getLastManagedDSL(
            String token, UUID projectID) {

    }

    @Override public void getConfig(
            String token,
            UUID projectID,
            Set<String> targets,
            String packageName,
            Set<String> options) {

    }

    @Override public void updateManagedProject(
            String token,
            UUID projectID,
            Set<String> targets,
            String packageName,
            String migration,
            Set<String> options,
            Map<String, String> dsl) {

    }

    @Override public void generateMigrationSQL(
            String token,
            String version,
            Map<String, String> oldDsl,
            Map<String, String> newDsl) {

    }

    @Override public void generateSources(
            String token,
            UUID projectID,
            Set<String> targets,
            String packageName,
            Set<String> options) {

    }

    @Override public void generateUnmanagedSources(
            String token,
            String packageName,
            Set<String> targets,
            Set<String> options,
            Map<String, String> dsl) {

    }

    @Override public void getProjectByName(
            String token, String projectName) {

    }

    @Override public void getAllProjects(
            String token) {

    }

    @Override public void renameProject(
            String token, String oldName, String newName) {

    }

    @Override public void cleanProject(
            String token) {

    }

    @Override public void templateGet(
            String token, String templateName) {

    }

    @Override public void templateCreate(
            String token,
            String templateName,
            byte[] content) {

    }

    @Override public void templateListAll(
            String token, UUID projectID) {

    }

    @Override public void templateDelete(
            String token, String templateName) {

    }

    @Override public boolean doesUnmanagedDSLExits(DataSource dataSource) {
        return false;
    }

    @Override public GetAllUnmanagedDSLResponse getAllUnmanagedDSL(
            DataSource dataSource) {
        final List<Migration> migrations;
        try {
            migrations = unmanagedDSL.getAllUnmanagedDSL(dataSource);
        } catch (SQLException e) {
            return new GetAllUnmanagedDSLResponse(false, e.getMessage(), null);
        }
        return new GetAllUnmanagedDSLResponse(true, null, migrations);
    }

    @Override public GetLastUnmanagedDSLResponse getLastUnmanagedDSL(
            final DataSource dataSource) {
        final Migration migration;
        try {
            migration = unmanagedDSL.getLastUnmanagedDSL(dataSource);
        } catch (SQLException e) {
            return new GetLastUnmanagedDSLResponse(false, e.getMessage(), null);
        }
        return new GetLastUnmanagedDSLResponse(true, null, migration);
    }

    @Override public void inspectUnmanagedProjectChanges(
            DataSource dataSource, String version, Map<String, String> dsl) {

    }

    @Override public void createUnmanagedProject(
            String token, DataSource dataSource, String serverName, String applicationName) {

    }

    @Override public void upgradeUnmanagedDatabase(
            DataSource dataSource,
            String version,
            List<String> migration) {

    }
}
