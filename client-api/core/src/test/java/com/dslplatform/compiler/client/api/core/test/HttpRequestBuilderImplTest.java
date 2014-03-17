package com.dslplatform.compiler.client.api.core.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

import javax.xml.bind.DatatypeConverter;

import org.junit.BeforeClass;
import org.junit.Test;

import com.dslplatform.compiler.client.api.core.HttpRequest;
import com.dslplatform.compiler.client.api.core.HttpRequestBuilder;
import com.dslplatform.compiler.client.api.core.impl.HttpRequestBuilderImpl;

public class HttpRequestBuilderImplTest {
    private static final Charset ENCODING = Charset.forName("UTF-8");

    private static HttpRequestBuilder httpRequestBuilder;
    private final String testUser = "ocd@dsl-platform.com";
    private final String testPassword = "ocdcdo";
    private final String projectID = "0d713347-d2d1-4e0a-b8db-97c4da41804f";
    final String testUserToken = "Basic " + DatatypeConverter.printBase64Binary((testUser + ":" + testPassword).getBytes(ENCODING));
    final String testProjectToken = "Basic " + DatatypeConverter.printBase64Binary((testUser + ":" + testPassword + ":" + projectID).getBytes(ENCODING));

    @BeforeClass
    public static void createHttpRequestBuilder() {
        httpRequestBuilder = new HttpRequestBuilderImpl();
    }

    @Test
    @SuppressWarnings("serial")
    public void testParseDslBuilder() throws IOException {
        final HttpRequest parseRequest; {
            final Map<String, String> dsl = new HashMap<String, String>();
            dsl.put("model.dsl", "module Foo {\n" +
                                   "\taggregate Bar { String baz; }\n" +
                                 "}");
            parseRequest = httpRequestBuilder.parseDsl(testUserToken, dsl);
        }

        assertEquals(HttpRequest.Method.PUT, parseRequest.method);
        assertEquals("Alpha.svc/parse", parseRequest.path);
        assertEquals(parseRequest.headers, new HashMap<String, List<String>>() {{
            put("Content-Type", Arrays.asList("application/json"));
            put("Accept", Arrays.asList("application/json"));
            put("Authorization", Arrays.asList(testUserToken));
        }});

        assertArrayEquals(
                "{\"model.dsl\":\"module Foo {\\n\\taggregate Bar { String baz; }\\n}\"}".getBytes(ENCODING),
                parseRequest.body);
    }

    @Test
    @SuppressWarnings("serial")
    public void testRenameProjectBuilder() throws IOException {
        final HttpRequest renameRequest; {
            final String token = "Basic " + DatatypeConverter.printBase64Binary("ocd@dsl-platform.com:xxx".getBytes(ENCODING));
            final String oldName = "GreenLeopard";
            final String newName = "GreenLion";
            renameRequest = httpRequestBuilder.renameProject(token, oldName, newName);
        }

        assertEquals(HttpRequest.Method.POST, renameRequest.method);
        assertEquals("Domain.svc/submit/Client.RenameProject", renameRequest.path);
        assertEquals(renameRequest.headers, new HashMap<String, List<String>>() {{
            put("Content-Type", Arrays.asList("application/json"));
            put("Accept", Arrays.asList("application/json"));
            put("Authorization", Arrays.asList("Basic " + DatatypeConverter.printBase64Binary("ocd@dsl-platform.com:xxx".getBytes(ENCODING))));
        }});
        assertArrayEquals(
                "{\"OldName\":\"GreenLeopard\",\"NewName\":\"GreenLion\"}".getBytes(ENCODING),
                renameRequest.body);
    }

    @Test
    @SuppressWarnings("serial")
    public void testRegisterUserBuilder() throws IOException {
        final HttpRequest registerUserRequest; {
            final String email = "user@test.org";
            registerUserRequest = httpRequestBuilder.registerUser(email);
        }

        assertEquals(HttpRequest.Method.POST, registerUserRequest.method);
        assertEquals("Domain.svc/submit/Client.Register", registerUserRequest.path);
        assertEquals(registerUserRequest.headers, new HashMap<String, List<String>>() {{
            put("Content-Type", Arrays.asList("application/json"));
            put("Accept", Arrays.asList("application/json"));
        }});
        assertArrayEquals(
                "{\"Email\":\"user@test.org\"}".getBytes(ENCODING),
                registerUserRequest.body);
    }

    @Test
    @SuppressWarnings("serial")
    public void testCreateProjectBuilder() throws IOException {
        final HttpRequest createProjectRequest; {
            final String token = "Basic " + DatatypeConverter.printBase64Binary("ocd@dsl-platform.com:xxx".getBytes(ENCODING));
            final String projectName = "NewProjectName";
            createProjectRequest = httpRequestBuilder.createTestProject(token, projectName);
        }

        assertEquals(HttpRequest.Method.POST, createProjectRequest.method);
        assertEquals("Domain.svc/submit/Client.CreateProject", createProjectRequest.path);
        assertEquals(createProjectRequest.headers, new HashMap<String, List<String>>() {{
            put("Content-Type", Arrays.asList("application/json"));
            put("Accept", Arrays.asList("application/json"));
            put("Authorization", Arrays.asList("Basic " + DatatypeConverter.printBase64Binary("ocd@dsl-platform.com:xxx".getBytes(ENCODING))));
        }});
        assertArrayEquals(
                "{\"ProjectName\":\"NewProjectName\"}".getBytes(ENCODING),
                createProjectRequest.body);
    }

    @Test
    @SuppressWarnings("serial")
    public void testCreateExternalProjectBuilder() throws IOException {
        final HttpRequest createExternalProjectRequest; {
            final String projectName = "NewProjectName";
            final String serverName = "someServerName";
            final String applicationName = "someApplicationName";
            final Map<String, Object> databaseConnection = new LinkedHashMap<String, Object>();
            databaseConnection.put("Server", "someServer");
            databaseConnection.put("Port", 4);
            databaseConnection.put("Database", "someDatabase");
            databaseConnection.put("Username", "someUsername");
            databaseConnection.put("Password", "somePassword");
            createExternalProjectRequest = httpRequestBuilder.createExternalProject(testUserToken, projectName, serverName, applicationName, databaseConnection);
        }

        assertEquals(HttpRequest.Method.POST, createExternalProjectRequest.method);
        assertEquals("Domain.svc/submit/Client.CreateExternalProject", createExternalProjectRequest.path);
        assertEquals(createExternalProjectRequest.headers, new HashMap<String, List<String>>() {{
            put("Content-Type", Arrays.asList("application/json"));
            put("Accept", Arrays.asList("application/json"));
            put("Authorization", Arrays.asList(testUserToken));
        }});

        assertArrayEquals(
            "{\"ProjectName\":\"NewProjectName\",\"ServerName\":\"someServerName\",\"ApplicationName\":\"someApplicationName\",\"DatabaseConnection\":{\"Server\":\"someServer\",\"Port\":4,\"Database\":\"someDatabase\",\"Username\":\"someUsername\",\"Password\":\"somePassword\"}}"
                    .getBytes(ENCODING),
            createExternalProjectRequest.body);
    }

    @Test
    @SuppressWarnings("serial")
    public void testDownloadBinariesBuilder() throws IOException {
        final HttpRequest downloadBinariesRequest; {
            downloadBinariesRequest = httpRequestBuilder.downloadBinaries(testProjectToken, UUID.fromString(projectID));
        }

        assertEquals(HttpRequest.Method.GET, downloadBinariesRequest.method);
        assertEquals("Alpha.svc/download/" + projectID, downloadBinariesRequest.path);
        assertEquals(downloadBinariesRequest.headers, new HashMap<String, List<String>>() {{
            put("Content-Type", Arrays.asList("application/json"));
            put("Accept", Arrays.asList("application/json"));
            put("Authorization", Arrays.asList(testProjectToken));
        }});
    }

    @Test
    @SuppressWarnings("serial")
    public void testDownloadGeneratedSourceBuilder() throws IOException {
        final HttpRequest downloadGeneratedSourceRequest; {
            downloadGeneratedSourceRequest = httpRequestBuilder.downloadGeneratedModel(testProjectToken, UUID.fromString(projectID));
        }

        assertEquals(HttpRequest.Method.GET, downloadGeneratedSourceRequest.method);
        assertEquals("Alpha.svc/generated-model/" + projectID, downloadGeneratedSourceRequest.path);
        assertEquals(downloadGeneratedSourceRequest.headers, new HashMap<String, List<String>>() {{
            put("Content-Type", Arrays.asList("application/json"));
            put("Accept", Arrays.asList("application/json"));
            put("Authorization", Arrays.asList(testProjectToken));
        }});
    }

    @Test
    @SuppressWarnings("serial")
    public void testInspectManagedProjectChanges() {
        final HttpRequest inspectManagedProjectRequest; {
            final Map<String, String> dsl = new LinkedHashMap<String, String>(){{
                put("only", "module A { root B; root C{ B b;}}");
            }};

            inspectManagedProjectRequest = httpRequestBuilder.inspectManagedProjectChanges(testProjectToken, UUID.fromString(projectID), dsl);
        }

        assertEquals(HttpRequest.Method.PUT, inspectManagedProjectRequest.method);
        assertEquals("Alpha.svc/changes/" + projectID, inspectManagedProjectRequest.path);
        assertEquals(inspectManagedProjectRequest.headers, new HashMap<String, List<String>>() {{
            put("Content-Type", Arrays.asList("application/json"));
            put("Accept", Arrays.asList("application/json"));
            put("Authorization", Arrays.asList(testProjectToken));
        }});
        assertArrayEquals(
                "{\"only\":\"module A { root B; root C{ B b;}}\"}".getBytes(ENCODING),
                inspectManagedProjectRequest.body);
    }

    @Test
    @SuppressWarnings("serial")
    public void testGetLastManagedDSL() {
        final HttpRequest getLastManagedDSLRequest; {
            getLastManagedDSLRequest = httpRequestBuilder.getLastManagedDSL(testProjectToken, UUID.fromString(projectID));
        }

        assertEquals(HttpRequest.Method.GET, getLastManagedDSLRequest.method);
        assertEquals("Alpha.svc/dsl/" + projectID, getLastManagedDSLRequest.path);
        assertEquals(getLastManagedDSLRequest.headers, new HashMap<String, List<String>>() {{
            put("Content-Type", Arrays.asList("application/json"));
            put("Accept", Arrays.asList("application/json"));
            put("Authorization", Arrays.asList(testProjectToken));
        }});
    }
    /*
        @Override
        @Test
    @SuppressWarnings("serial")
    public void testGetLastUnmanagedDSL() {


    }
    */

    @Test
    @SuppressWarnings("serial")
    public void testGetConfig() {
        final HttpRequest getConfigRequest; {
            final Set<String> targets = new HashSet<String>() {{
                add("java");
                add("scala");
            }};
            final Set<String> options = new HashSet<String>() {{
                add("opt1");
                add("opt2");
            }};
            final String packageName = "namespace";
            getConfigRequest = httpRequestBuilder.getConfig(testProjectToken, UUID.fromString(projectID), targets, packageName, options);
        }

        assertEquals(HttpRequest.Method.GET, getConfigRequest.method);
        assertEquals("Alpha.svc/config/" + projectID, getConfigRequest.path);
        assertEquals(getConfigRequest.headers, new HashMap<String, List<String>>() {{
            put("Content-Type", Arrays.asList("application/json"));
            put("Accept", Arrays.asList("application/json"));
            put("Authorization", Arrays.asList(testProjectToken));
        }});
        assertEquals(getConfigRequest.query, new HashMap<String, List<String>>() {{
            put("targets", Arrays.asList("java", "scala"));
            put("options", Arrays.asList("opt2", "opt1"));
            put("namespace", Arrays.asList("namespace"));
        }});

    }

    @Test
    @SuppressWarnings("serial")
    public void testUpdateManagedProject() {
        final HttpRequest updateManagedRequest; {
            final Set<String> targets = new HashSet<String>() {{
                add("java");
                add("scala");
            }};
            final Set<String> options = new HashSet<String>() {{
                add("opt1");
                add("opt2");
            }};
            final String packageName = "namespace";
            final String migration = "migration";
            final Map<String, String> dsl = new LinkedHashMap<String, String>(){{
                put("only", "module A { root B; root C{ B b;}}");
            }};
            updateManagedRequest = httpRequestBuilder.updateManagedProject(testProjectToken, UUID.fromString(projectID), targets, packageName, migration, options, dsl);
        }

        assertEquals(HttpRequest.Method.PUT, updateManagedRequest.method);
        assertEquals("Alpha.svc/update/" + projectID, updateManagedRequest.path);
        assertEquals(updateManagedRequest.headers, new HashMap<String, List<String>>() {{

            put("Authorization", Arrays.asList(testProjectToken));
            put("Content-Type", Arrays.asList("application/json"));
            put("Accept", Arrays.asList("application/json"));
        }});
        assertEquals(updateManagedRequest.query, new HashMap<String, List<String>>() {{
            put("targets", Arrays.asList("java", "scala"));
            put("options", Arrays.asList("opt2", "opt1"));
            put("namespace", Arrays.asList("namespace"));
            put("migration", Arrays.asList("migration"));
        }});
        assertArrayEquals(
                "{\"only\":\"module A { root B; root C{ B b;}}\"}".getBytes(ENCODING),
                updateManagedRequest.body);
    }

    @Test
    @SuppressWarnings("serial")
    public void testGenerateMigrationSQL() {
        final HttpRequest generateMigrationSQLRequest; {

            final Map<String, String> olddsl = new LinkedHashMap<String, String>(){{
                put("only", "module A { root B; root C{ B b;}}");
            }};
            final Map<String, String> newdsl = new LinkedHashMap<String, String>(){{
                put("only", "module A { root B; root C{ B b;}; root AddedRoot;}");
            }};
            final String version = "someversion";
            generateMigrationSQLRequest = httpRequestBuilder.generateMigrationSQL(testUserToken, version, olddsl, newdsl);
        }

        assertEquals(HttpRequest.Method.PUT, generateMigrationSQLRequest.method);
        assertEquals("Alpha.svc/unmanaged/postgres-migration", generateMigrationSQLRequest.path);
        assertEquals(generateMigrationSQLRequest.headers, new HashMap<String, List<String>>() {{
            put("version", Arrays.asList("someversion"));

            put("Authorization", Arrays.asList(testUserToken));
            put("Content-Type", Arrays.asList("application/json"));
            put("Accept", Arrays.asList("application/json"));

        }});

        assertArrayEquals(
                "{\"NewDsl\":{\"only\":\"module A { root B; root C{ B b;}; root AddedRoot;}\"},\"OldDsl\":{\"only\":\"module A { root B; root C{ B b;}}\"}}".getBytes(ENCODING),
                generateMigrationSQLRequest.body);
    }

    @Test
    @SuppressWarnings("serial")
    public void testGenerateSources() {
        final HttpRequest updateManagedRequest; {
            final Set<String> targets = new HashSet<String>() {{
                add("java");
                add("scala");
            }};
            final Set<String> options = new HashSet<String>() {{
                add("opt1");
                add("opt2");
            }};
            final String packageName = "namespace";
            updateManagedRequest = httpRequestBuilder.generateSources(testProjectToken, UUID.fromString(projectID), targets, packageName, options);
        }

        assertEquals(HttpRequest.Method.GET, updateManagedRequest.method);
        assertEquals("Alpha.svc/source/" + projectID, updateManagedRequest.path);
        assertEquals(updateManagedRequest.headers, new HashMap<String, List<String>>() {{
            put("Authorization", Arrays.asList(testProjectToken));
            put("Content-Type", Arrays.asList("application/json"));
            put("Accept", Arrays.asList("application/json"));
        }});
        assertEquals(updateManagedRequest.query, new HashMap<String, List<String>>() {{
            put("targets", Arrays.asList("java", "scala"));
            put("options", Arrays.asList("opt2", "opt1"));
            put("namespace", Arrays.asList("namespace"));
        }});
    }

    @Test
    @SuppressWarnings("serial")
    public void testGenerateUnmanagedSources() {
        final HttpRequest generateUnmanagedRequest; {
            final Set<String> targets = new HashSet<String>() {{
                add("java");
                add("scala");
            }};
            final Set<String> options = new HashSet<String>() {{
                add("opt1");
                add("opt2");
            }};
            final String packageName = "namespace";
            final Map<String, String> dsl = new LinkedHashMap<String, String>(){{
                put("only", "module A { root B; root C{ B b;}}");
            }};
            generateUnmanagedRequest = httpRequestBuilder.generateUnmanagedSources(testUserToken, packageName, targets, options, dsl);
        }

        assertEquals(HttpRequest.Method.PUT, generateUnmanagedRequest.method);
        assertEquals("Alpha.svc/unmanaged/source", generateUnmanagedRequest.path);

        assertEquals(generateUnmanagedRequest.headers,  new HashMap<String, List<String>>() {{
            put("Authorization", Arrays.asList(testUserToken));
            put("Content-Type", Arrays.asList("application/json"));
            put("Accept", Arrays.asList("application/json"));
        }});

        assertEquals(generateUnmanagedRequest.query, new HashMap<String, List<String>>() {{
            put("targets", Arrays.asList("java", "scala"));
            put("options", Arrays.asList("opt2", "opt1"));
            put("namespace", Arrays.asList("namespace"));
        }});
        assertArrayEquals(
                "{\"only\":\"module A { root B; root C{ B b;}}\"}".getBytes(ENCODING),
                generateUnmanagedRequest.body);
    }

    @Test
    @SuppressWarnings("serial")
    public void testGetProjectByName() {
        final HttpRequest getProjectByNameRequest; {
            final String projectName =  "projectName";
            getProjectByNameRequest = httpRequestBuilder.getProjectByName(testUserToken, projectName);
        }

        assertEquals(HttpRequest.Method.PUT, getProjectByNameRequest.method);
        assertEquals("Domain.svc/search/Client.Project", getProjectByNameRequest.path);
        assertEquals(getProjectByNameRequest.headers, new HashMap<String, List<String>>() {{
            put("specification", Arrays.asList("FindByUserAndName"));
            put("Authorization", Arrays.asList(testUserToken));
            put("Content-Type", Arrays.asList("application/json"));
            put("Accept", Arrays.asList("application/json"));
        }});

        assertArrayEquals(
                "{\"Name\":\"projectName\"}".getBytes(ENCODING),
                getProjectByNameRequest.body);
    }

    @Test
    @SuppressWarnings("serial")
    public void testGetAllProjects() {
        final HttpRequest getAllProjectsRequest; {
            getAllProjectsRequest = httpRequestBuilder.getAllProjects(testUserToken);
        }

        assertEquals(HttpRequest.Method.GET, getAllProjectsRequest.method);
        assertEquals("Domain.svc/search/Client.Project", getAllProjectsRequest.path);
        assertEquals(getAllProjectsRequest.headers, new HashMap<String, List<String>>() {{
            put("Authorization", Arrays.asList(testUserToken));
            put("Content-Type", Arrays.asList("application/json"));
            put("Accept", Arrays.asList("application/json"));
        }});
    }

    @Test
    @SuppressWarnings("serial")
    public void testCleanProject() {
        final HttpRequest getAllProjectsRequest; {
            getAllProjectsRequest = httpRequestBuilder.cleanProject(testProjectToken);
        }

        assertEquals(HttpRequest.Method.POST, getAllProjectsRequest.method);
        assertEquals("Domain.svc/submit/Client.CleanProject", getAllProjectsRequest.path);
        assertEquals(getAllProjectsRequest.headers, new HashMap<String, List<String>>() {{
            put("Authorization", Arrays.asList(testProjectToken));
            put("Content-Type", Arrays.asList("application/json"));
            put("Accept", Arrays.asList("application/json"));
        }});
    }

    @Test
    @SuppressWarnings("serial")
    public void testTemplateGet() {
        final HttpRequest getAllProjectsRequest; {
            final String templateName = "templateName";
            getAllProjectsRequest = httpRequestBuilder.templateGet(testProjectToken, templateName);
        }

        assertEquals(HttpRequest.Method.GET, getAllProjectsRequest.method);
        assertEquals("Alpha.svc/template/templateName", getAllProjectsRequest.path);
        assertEquals(getAllProjectsRequest.headers, new HashMap<String, List<String>>() {{
            put("Authorization", Arrays.asList(testProjectToken));
            put("Content-Type", Arrays.asList("application/json"));
            put("Accept", Arrays.asList("application/json"));
        }});
    }

    @Test
    @SuppressWarnings("serial")
    public void testTemplateCreate() throws IOException {
        final HttpRequest templateCreateRequest; {
            final String templateName = "templateName";
            final byte [] templateContent = "templateContent".getBytes("UTF-8");
            templateCreateRequest = httpRequestBuilder.templateCreate(testProjectToken, templateName, templateContent);
        }

        assertEquals(HttpRequest.Method.POST, templateCreateRequest.method);
        assertEquals("Domain.svc/submit/Client.UploadTemplate", templateCreateRequest.path);
        assertEquals(templateCreateRequest.headers, new HashMap<String, List<String>>() {{
            put("Authorization", Arrays.asList(testProjectToken));
            put("Content-Type", Arrays.asList("application/json"));
            put("Accept", Arrays.asList("application/json"));
        }});

        assertArrayEquals(
                "{\"Name\":\"templateName\",\"Content\":\"dGVtcGxhdGVDb250ZW50\"}".getBytes(ENCODING),
                templateCreateRequest.body);
    }

    @Test
    @SuppressWarnings("serial")
    public void testTemplateListAll() {
        final HttpRequest templateListAllRequest; {
            templateListAllRequest = httpRequestBuilder.templateListAll(testProjectToken, UUID.fromString(projectID));
        }

        assertEquals(HttpRequest.Method.GET, templateListAllRequest.method);
        assertEquals("Alpha.svc/templates/" + projectID, templateListAllRequest.path);
        assertEquals(templateListAllRequest.headers, new HashMap<String, List<String>>() {{
            put("Authorization", Arrays.asList(testProjectToken));
            put("Content-Type", Arrays.asList("application/json"));
            put("Accept", Arrays.asList("application/json"));
        }});
    }

    @Test
    @SuppressWarnings("serial")
    public void testTemplateDelete() {
        final HttpRequest templateDeleteRequest; {
            final String templateToDelete = "templateToDeleteName";
            templateDeleteRequest = httpRequestBuilder.templateDelete(testProjectToken, templateToDelete);
        }

        assertEquals(HttpRequest.Method.POST, templateDeleteRequest.method);
        assertEquals("Domain.svc/submit/Client.DeleteTemplate", templateDeleteRequest.path);
        assertEquals(templateDeleteRequest.headers, new HashMap<String, List<String>>() {{
            put("Authorization", Arrays.asList(testProjectToken));
            put("Content-Type", Arrays.asList("application/json"));
            put("Accept", Arrays.asList("application/json"));
        }});

        assertArrayEquals(
                "{\"Name\":\"templateToDeleteName\"}".getBytes(ENCODING),
                templateDeleteRequest.body);
    }
}
