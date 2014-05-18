package com.dslplatform.compiler.client.api.core.impl;

import java.util.*;

import com.dslplatform.compiler.client.api.core.HttpRequestBuilder;
import com.dslplatform.compiler.client.api.core.HttpRequest;

public class HttpRequestBuilderImpl implements HttpRequestBuilder {
    @Override
    public HttpRequest parseDSL(final String token, final Map<String, String> dsl) {
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
    public HttpRequest registerUser(final String email) {
        final Map<String, String> event = new LinkedHashMap<String, String>();
        if (email != null) event.put("Email", email);

        final HttpRequest request = HttpRequest.POST("Domain.svc/submit/Client.Register", event);

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
            final Map<String, Object> databaseConnection) {
        final Map<String, Object> event = new LinkedHashMap<String, Object>();
        if (projectName != null) event.put("ProjectName", projectName);
        if (serverName != null) event.put("ServerName", serverName);
        if (applicationName != null) event.put("ApplicationName", applicationName);
        if (databaseConnection != null) event.put("Database", databaseConnection);

        final HttpRequest request = HttpRequest.POST("Domain.svc/submit/Client.CreateExternalProject", event);

        request.headers.put("Authorization", Arrays.asList(token));
        request.headers.put("Content-Type", Arrays.asList("application/json"));
        request.headers.put("Accept", Arrays.asList("application/json"));
        return request;
    }

    @Override
    public HttpRequest downloadBinaries(final String token, final UUID projectID) {
        final HttpRequest request = HttpRequest.GET("Alpha.svc/download/" + projectID.toString());

        request.headers.put("Authorization", Arrays.asList(token));
        request.headers.put("Content-Type", Arrays.asList("application/json"));
        request.headers.put("Accept", Arrays.asList("application/json"));
        return request;
    }

    @Override
    public HttpRequest downloadGeneratedModel(final String token, final UUID projectID) {
        final HttpRequest request = HttpRequest.GET("Alpha.svc/generated-model/" + projectID.toString());

        request.headers.put("Authorization", Arrays.asList(token));
        request.headers.put("Content-Type", Arrays.asList("application/json"));
        request.headers.put("Accept", Arrays.asList("application/json"));
        return request;
    }

    @Override
    public HttpRequest inspectManagedProjectChanges(
            final String token,
            final UUID projectID,
            final Map<String, String> dsl) {
        final HttpRequest request = HttpRequest.PUT("Alpha.svc/changes/" + projectID.toString(), dsl);

        request.headers.put("Authorization", Arrays.asList(token));
        request.headers.put("Content-Type", Arrays.asList("application/json"));
        request.headers.put("Accept", Arrays.asList("application/json"));
        return request;
    }

    @Override
    public HttpRequest getLastManagedDSL(final String token, final UUID projectID) {
        final HttpRequest request = HttpRequest.GET("Alpha.svc/dsl/" + projectID.toString());

        request.headers.put("Authorization", Arrays.asList(token));
        request.headers.put("Content-Type", Arrays.asList("application/json"));
        request.headers.put("Accept", Arrays.asList("application/json"));
        return request;
    }

    @Override
    public HttpRequest getConfig(
            final String token,
            final UUID projectID,
            final Set<String> targets,
            final String packageName,
            final Set<String> options) {
        final HttpRequest request = HttpRequest.GET("Alpha.svc/config/" + projectID.toString());

        if (targets != null && !targets.isEmpty()) request.query.put("targets", new ArrayList<String>(targets));
        if (options != null && !options.isEmpty()) request.query.put("options", new ArrayList<String>(options));
        if (packageName != null) request.query.put("namespace", Arrays.asList(packageName));
        request.headers.put("Authorization", Arrays.asList(token));
        request.headers.put("Content-Type", Arrays.asList("application/json"));
        request.headers.put("Accept", Arrays.asList("application/json"));
        return request;
    }

    @Override
    public HttpRequest updateManagedProject(
            final String token,
            final UUID projectID,
            final Set<String> targets,
            final String packageName,
            final String migration,
            final Set<String> options,
            final Map<String, String> dsl
    ) {
        final HttpRequest request = HttpRequest.POST("Alpha.svc/update/" + projectID.toString(), dsl);

        if (targets != null && !targets.isEmpty()) request.query.put("targets", new ArrayList<String>(targets));
        if (options != null && !options.isEmpty()) request.query.put("options", new ArrayList<String>(options));
        if (migration != null) request.query.put("migration", Arrays.asList(migration));
        if (packageName != null) request.query.put("namespace", Arrays.asList(packageName));
        request.headers.put("Authorization", Arrays.asList(token));
        request.headers.put("Content-Type", Arrays.asList("application/json"));
        request.headers.put("Accept", Arrays.asList("application/json"));
        return request;
    }

    @Override
    public HttpRequest generateMigrationSQL(
            final String token,
            final String version,
            final Map<String, String> oldDsl,
            final Map<String, String> newDsl) {

        if (oldDsl == null || newDsl == null)
            throw new IllegalArgumentException("New and old dsl must be provided.");

        final Map<String, Object> dsl = new LinkedHashMap<String, Object>();
        dsl.put("New", newDsl);
        dsl.put("Old", oldDsl);

        final HttpRequest request = HttpRequest.PUT("Alpha.svc/unmanaged/postgres-migration", dsl);

        request.query.put("version", Arrays.asList(version));

        request.headers.put("Authorization", Arrays.asList(token));
        request.headers.put("Content-Type", Arrays.asList("application/json"));
        request.headers.put("Accept", Arrays.asList("application/json"));
        return request;
    }

    @Override
    public HttpRequest generateSources(
            final String token,
            final UUID projectID,
            final Set<String> targets,
            final String packageName,
            final Set<String> options) {
        final HttpRequest request = HttpRequest.GET("Alpha.svc/source/" + projectID.toString());

        if (targets != null && !targets.isEmpty()) request.query.put("targets", new ArrayList<String>(targets));
        if (options != null && !options.isEmpty()) request.query.put("options", new ArrayList<String>(options));
        if (packageName != null) request.query.put("namespace", Arrays.asList(packageName));
        request.headers.put("Authorization", Arrays.asList(token));
        request.headers.put("Content-Type", Arrays.asList("application/json"));
        request.headers.put("Accept", Arrays.asList("application/json"));
        return request;
    }

    @Override
    public HttpRequest generateUnmanagedSources(
            final String token,
            final String packageName,
            final Set<String> targets,
            final Set<String> options,
            final Map<String, String> dsl) {
        final HttpRequest request = HttpRequest.PUT("Alpha.svc/unmanaged/source", dsl);

        if (targets != null && !targets.isEmpty()) request.query.put("targets", new ArrayList<String>(targets));
        if (options != null && !options.isEmpty()) request.query.put("options", new ArrayList<String>(options));
        if (packageName != null) request.query.put("namespace", Arrays.asList(packageName));
        request.headers.put("Authorization", Arrays.asList(token));
        request.headers.put("Content-Type", Arrays.asList("application/json"));
        request.headers.put("Accept", Arrays.asList("application/json"));
        return request;
    }

    @Override
    public HttpRequest getProjectByName(final String token, final String projectName) {
        final Map<String, Object> dsl = new LinkedHashMap<String, Object>();
        dsl.put("Name", projectName);

        final HttpRequest request = HttpRequest.PUT("Domain.svc/search/Client.Project", dsl);

        request.query.put("specification", Arrays.asList("FindByName"));

        request.headers.put("Authorization", Arrays.asList(token));
        request.headers.put("Content-Type", Arrays.asList("application/json"));
        request.headers.put("Accept", Arrays.asList("application/json"));
        return request;
    }

    @Override
    public HttpRequest getAllProjects(final String token) {
        final HttpRequest request = HttpRequest.GET("Domain.svc/search/Client.Project");

        request.headers.put("Authorization", Arrays.asList(token));
        request.headers.put("Content-Type", Arrays.asList("application/json"));
        request.headers.put("Accept", Arrays.asList("application/json"));
        return request;
    }

    @Override
    public HttpRequest cleanProject(final String token) {
        final Map<String, Object> event = new LinkedHashMap<String, Object>();

        final HttpRequest request = HttpRequest.POST("Domain.svc/submit/Client.CleanProject", event);

        request.headers.put("Authorization", Arrays.asList(token));
        request.headers.put("Content-Type", Arrays.asList("application/json"));
        request.headers.put("Accept", Arrays.asList("application/json"));
        return request;
    }

    @Override
    public HttpRequest templateGet(final String token, final String projectId, final String templateName) {
        final HttpRequest request = HttpRequest.GET("Alpha.svc/template/" + projectId + "/" + templateName);

        request.headers.put("Authorization", Arrays.asList(token));
        request.headers.put("Content-Type", Arrays.asList("application/json"));
        request.headers.put("Accept", Arrays.asList("application/json"));
        return request;
    }

    @Override
    public HttpRequest templateCreate(final String token, final String templateName, final byte[] content) {
        final Map<String, Object> event = new LinkedHashMap<String, Object>();
        event.put("Name", templateName);
        event.put("Content", content);

        final HttpRequest request = HttpRequest.POST("Domain.svc/submit/Client.UploadTemplate", event);

        request.headers.put("Authorization", Arrays.asList(token));
        request.headers.put("Content-Type", Arrays.asList("application/json"));
        request.headers.put("Accept", Arrays.asList("application/json"));
        return request;
    }

    @Override
    public HttpRequest templateListAll(final String token, final UUID projectID) {
        final HttpRequest request = HttpRequest.GET("Alpha.svc/templates/" + projectID.toString());

        request.headers.put("Authorization", Arrays.asList(token));
        request.headers.put("Content-Type", Arrays.asList("application/json"));
        request.headers.put("Accept", Arrays.asList("application/json"));
        return request;
    }

    @Override
    public HttpRequest templateDelete(final String token, final String templateName) {
        final Map<String, Object> event = new LinkedHashMap<String, Object>();
        event.put("Name", templateName);

        final HttpRequest request = HttpRequest.POST("Domain.svc/submit/Client.DeleteTemplate", event);

        request.headers.put("Authorization", Arrays.asList(token));
        request.headers.put("Content-Type", Arrays.asList("application/json"));
        request.headers.put("Accept", Arrays.asList("application/json"));
        return request;
    }
}
