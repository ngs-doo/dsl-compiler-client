package com.dslplatform.test;

import static org.junit.Assert.assertTrue;

import com.dslplatform.compiler.client.api.core.ActionImpl;
import com.dslplatform.compiler.client.api.model.Client.DatabaseConnection;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import javax.crypto.Cipher;

public class CoreTestCase {

    String dsl = "{module  X { root T{ Int i;}}";
    String newdsl = "{module  X { root T{ Int i; Int j;}}";
    String testUserName = "some@user.dsl";
    String testPassword = "password";
    String version = "version";
    Set<String> targets = null;
    Set<String> options = null;

    UUID testProjectID = UUID.fromString("0d713347-d2d1-4e0a-b8db-97c4da41804f");
    String projectName = "someProjectName";
    String serverName = "someServerName";
    String applicationName = "someApplicationName";
    DatabaseConnection databaseConnection = new DatabaseConnection();

    @Test
    public void testParse() {
        ActionImpl ai = new ActionImpl(testUserName, "qwe321");
        ai.parseDsl(ai.makeToken(), dsl);
    }
    @Test
    public void testRegisterUser() throws Exception {
        ActionImpl ai = new ActionImpl(testUserName, "qwe321");
        ai.registerUser("fake@email");
    }

    @Test
    public void testCreateTestProject() {
        ActionImpl ai = new ActionImpl(testUserName, "qwe321");

        ai.createTestProject(ai.makeToken(), "testname" + new java.util.Random().nextInt(34));
    }
/*
    @Test
    public void testcreateExternalProject(byte[] token, UUID projectID, String serverName, String applicationName, DatabaseConnection databaseConnection) {
        CreateExternalProject cep = new CreateExternalProject();
    }
*/
    @Test
    public void testCreateExternalProject() {
        ActionImpl ai = new ActionImpl(testUserName, testPassword);

        ai.createExternalProject(ai.makeToken(), projectName, serverName, applicationName, databaseConnection);
    }

    @Test
    public void testDreateUnmanagedProject() {
        ActionImpl ai = new ActionImpl(testUserName, testPassword);

        ai.createUnmanagedProject(ai.makeToken(), serverName, applicationName, databaseConnection);
    }

    @Test
    public void testDownloadBinaries() {
        ActionImpl ai = new ActionImpl(testUserName, testPassword);

        ai.downloadBinaries(ai.makeToken(), testProjectID);

    }

    @Test
    public void testDownloadGeneratedModel() {
        ActionImpl ai = new ActionImpl(testUserName, testPassword);

        ai.downloadGeneratedModel(ai.makeToken(), testProjectID);
    }

    @Test
    public void testInspectUnmanagedProjectChanges() {
        ActionImpl ai = new ActionImpl(testUserName, testPassword);

        ai.inspectUnmanagedProjectChanges(ai.makeToken(), databaseConnection, newdsl, version);
    }

    @Test //Method = "PUT", UriTemplate = "/changes/{projectID}"
    public void testInspectProjectChanges() {
        ActionImpl ai = new ActionImpl(testUserName, testPassword);

        ai.inspectProjectChanges(ai.makeToken(), testProjectID,  newdsl);
    }

    @Test
    public void testGetLastManagedDSL() {
        ActionImpl ai = new ActionImpl(testUserName, testPassword);

        ai.getLastManagedDSL(ai.makeToken(), testProjectID);
    }

    @Test
    public void testGetLastUnManagedDSL() {
        ActionImpl ai = new ActionImpl(testUserName, testPassword);

        ai.getLastUnManagedDSL(ai.makeToken(), databaseConnection);
    }

    @Test // /config/{projectID}?targets={targets}&namespace={ns}&options={options}"
    public void testGetConfig() {
        ActionImpl ai = new ActionImpl(testUserName, testPassword);

        String targets = "Java";
        String options = "";
        String ns = "";
        ai.getConfig(ai.makeToken(), testProjectID, null, ns, null);
    }

    @Test
    public void testDiffWithLastDslUnmanaged() {
        ActionImpl ai = new ActionImpl(testUserName, testPassword);

        ai.diffWithLastDslUnmanaged(ai.makeToken(), databaseConnection);
    }

    @Test
    public void testDiffWithLastDslManaged() {
        ActionImpl ai = new ActionImpl(testUserName, testPassword);

        ai.diffWithLastDslManaged(ai.makeToken(), testProjectID);
    }

    @Test
    public void testUpdateManagedProject() {
        String namespace = "test";
        String migration = null;
        String dsl = "";
        Set<String> targets = null;
        Set<String> options = null;
        ActionImpl ai = new ActionImpl(testUserName, testPassword);

        ai.updateManagedProject(ai.makeToken(), testProjectID, namespace, migration, dsl, targets, options);
    }

    @Test
    public void testUpdateUnManagedProject() {
        String namespace = "test";
        Set<String> targets = null;
        Set<String> options = null;

        ActionImpl ai = new ActionImpl(testUserName, testPassword);

        ai.updateUnManagedProject(ai.makeToken(), databaseConnection, namespace, dsl,  targets, options);
    }

    @Test
    public void testGenerateMigrationSQL() {
        ActionImpl ai = new ActionImpl(testUserName, testPassword);

        ai.generateMigrationSQL(ai.makeToken(), dsl, newdsl, version);

    }

    @Test
    public void testGenerateSources() {
        Set<String> targets = null;
        Set<String> options = null;
        String namespace = "test";

        ActionImpl ai = new ActionImpl(testUserName, testPassword);

        ai.generateSources(ai.makeToken(), testProjectID, namespace, targets, options);
    }

    @Test
    public void testGenerateUnmanagedSources() {
        Set<String> targets = null;
        Set<String> options = null;
        String namespace = "test";

        ActionImpl ai = new ActionImpl(testUserName, testPassword);

        ai.generateUnmanagedSources(ai.makeToken(), namespace, targets, options, dsl);
    }

    @Test
    public void testGetProjectByName() {
        ActionImpl ai = new ActionImpl(testUserName, testPassword);

        ai.getProjectByName(ai.makeToken(), "testName");
    }

    @Test
    public void testGetAllProjects() {
        ActionImpl ai = new ActionImpl(testUserName, testPassword);

        ai.getAllProjects(ai.makeToken());
    }

    @Test
    public void testRenameProject() {
        ActionImpl ai = new ActionImpl(testUserName, testPassword);
        String oldName = "Some Old Project Name.";
        String newName = "Some New Project Name.";

        ai.renameProject(ai.makeToken(), oldName, newName);
    }

    @Test
    public void testCleanProject() {
        ActionImpl ai = new ActionImpl(testUserName, testPassword);

        ai.cleanProject(ai.makeToken(testProjectID.toString()));
    }

    @Test
    public void testTemplateGet() {
        ActionImpl ai = new ActionImpl(testUserName, testPassword);
        String templateName = "template Name";

        ai.templateGet(ai.makeToken(), templateName);
    }

    @Test
    public void testTemplateCreate() {
        ActionImpl ai = new ActionImpl(testUserName, testPassword);
        String templateName = "template Name";

        ai.templateCreate(ai.makeToken(), templateName);
    }

    @Test
    public void testTemplateListAll() {
        String templateName = "template Name";
        ActionImpl ai = new ActionImpl(testUserName, testPassword);

        ai.templateListAll(ai.makeToken(), templateName);
    }

    @Test
    public void testTemplateDelete() {
        String templateName = "template Name";
        ActionImpl ai = new ActionImpl(testUserName, testPassword);

        ai.templateDelete(ai.makeToken(), templateName);
    }
}
