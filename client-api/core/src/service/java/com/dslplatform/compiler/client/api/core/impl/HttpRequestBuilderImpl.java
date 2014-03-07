package com.dslplatform.compiler.client.api.core.impl;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import com.dslplatform.compiler.client.api.core.DatabaseConnection;
import com.dslplatform.compiler.client.api.core.HttpRequest;

public class HttpRequestBuilderImpl implements HttpRequestBuilder {
    @Override
    public HttpRequest parseDsl(final String token, final Map<String, String> dsl) {
        final HttpRequest request = HttpRequest.PUT("Alpha.svc/parse", dsl);
        request.headers.put("Authorization", Arrays.asList(token));
        request.headers.put("Content-Type", Arrays.asList("application/json"));
        request.headers.put("Accept", Arrays.asList("application/json"));
        return request;
    }

    @Override
    public HttpRequest renameProject(final String token, final String oldName, final String newName) {
        final Map<String, String> event = new LinkedHashMap<String, String>();
        if (oldName != null) event.put("OldName", oldName);
        if (newName != null) event.put("NewName", newName);

        final HttpRequest request = HttpRequest.POST("Domain.svc/submit/Client.RenameProject", event);
        request.headers.put("Authorization", Arrays.asList(token));
        request.headers.put("Content-Type", Arrays.asList("application/json"));
        request.headers.put("Accept", Arrays.asList("application/json"));
        return request;
    }

    @Override
    public HttpRequest registerUser(final String token, final String email) {
        final Map<String, String> event = new LinkedHashMap<String, String>();
        if (email != null) event.put("Email", email);

        final HttpRequest request = HttpRequest.POST("Domain.svc/submit/Client.Register", event);
        request.headers.put("Authorization", Arrays.asList(token));
        request.headers.put("Content-Type", Arrays.asList("application/json"));
        request.headers.put("Accept", Arrays.asList("application/json"));
        return request;
    }

    @Override
    public HttpRequest createTestProject(final String token, final String projectName) {
        final Map<String, String> event = new LinkedHashMap<String, String>();
        if (projectName != null) event.put("ProjectName", projectName);

        final HttpRequest request = HttpRequest.POST("Domain.svc/submit/Client.CreateProject", event);
        request.headers.put("Authorization", Arrays.asList(token));
        request.headers.put("Content-Type", Arrays.asList("application/json"));
        request.headers.put("Accept", Arrays.asList("application/json"));
        return request;
    }

    @Override
    public HttpRequest createExternalProject(
            final String token,
            final String projectName,
            final String serverName,
            final String applicationName,
            final DatabaseConnection databaseConnection) {
        // TODO Auto-generated method stub
        throw new NotImplementedException();
    }

    @Override
    public HttpRequest createUnmanagedProject(
            final String token,
            final String serverName,
            final String applicationName,
            final DatabaseConnection databaseConnection) {
        // TODO Auto-generated method stub
        throw new NotImplementedException();
    }

    @Override
    public HttpRequest downloadBinaries(final String token, final UUID projectID) {
        // TODO Auto-generated method stub
        throw new NotImplementedException();
    }

    @Override
    public HttpRequest downloadGeneratedModel(final String token, final UUID projectID) {
        // TODO Auto-generated method stub
        throw new NotImplementedException();
    }

    @Override
    public HttpRequest inspectUnmanagedProjectChanges(
            final String token,
            final DatabaseConnection databaseConnection,
            final String version,
            final Map<String, String> dsl) {
        // TODO Auto-generated method stub
        throw new NotImplementedException();
    }

    @Override
    public HttpRequest inspectManagedProjectChanges(
            final String token,
            final UUID projectID,
            final Map<String, String> dsl) {
        // TODO Auto-generated method stub
        throw new NotImplementedException();
    }

    @Override
    public HttpRequest getLastManagedDSL(final String token, final UUID projectID) {
        // TODO Auto-generated method stub
        throw new NotImplementedException();
    }

    @Override
    public HttpRequest getLastUnmanagedDSL(final String token, final DatabaseConnection databaseConnection) {
        // TODO Auto-generated method stub
        throw new NotImplementedException();
    }

    @Override
    public HttpRequest getConfig(
            final String token,
            final UUID projectID,
            final String packageName,
            final Set<String> targets,
            final Set<String> options) {
        // TODO Auto-generated method stub
        throw new NotImplementedException();
    }

    @Override
    public HttpRequest updateManagedProject(
            final String token,
            final UUID projectID,
            final String packageName,
            final Map<String, String> dsl,
            final Set<String> targets,
            final Set<String> options) {
        // TODO Auto-generated method stub
        throw new NotImplementedException();
    }

    @Override
    public HttpRequest generateMigrationSQL(
            final String token,
            final String version,
            final Map<String, String> oldDsl,
            final Map<String, String> newDsl) {
        // TODO Auto-generated method stub
        throw new NotImplementedException();
    }

    @Override
    public HttpRequest generateSources(
            final String token,
            final UUID projectID,
            final String packageName,
            final Set<String> targets,
            final Set<String> options) {
        // TODO Auto-generated method stub
        throw new NotImplementedException();
    }

    @Override
    public HttpRequest generateUnmanagedSources(
            final String token,
            final String packageName,
            final Set<String> targets,
            final Set<String> options,
            final Map<String, String> dsl) {
        // TODO Auto-generated method stub
        throw new NotImplementedException();
    }

    @Override
    public HttpRequest getProjectByName(final String token, final String projectName) {
        // TODO Auto-generated method stub
        throw new NotImplementedException();
    }

    @Override
    public HttpRequest getAllProjects(final String token) {
        // TODO Auto-generated method stub
        throw new NotImplementedException();
    }

    @Override
    public HttpRequest cleanProject(final String token) {
        // TODO Auto-generated method stub
        throw new NotImplementedException();
    }

    @Override
    public HttpRequest templateGet(final String token, final String templateName) {
        // TODO Auto-generated method stub
        throw new NotImplementedException();
    }

    @Override
    public HttpRequest templateCreate(final String token, final String templateName, final byte[] content) {
        // TODO Auto-generated method stub
        throw new NotImplementedException();
    }

    @Override
    public HttpRequest templateListAll(final String token, final String templateName) {
        // TODO Auto-generated method stub
        throw new NotImplementedException();
    }

    @Override
    public HttpRequest templateDelete(final String token, final String templateName) {
        // TODO Auto-generated method stub
        throw new NotImplementedException();
    }
}
