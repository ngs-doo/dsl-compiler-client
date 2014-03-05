/*
import com.dslplatform.compiler.client.api.model.Client.DatabaseConnection;
import com.dslplatform.compiler.client.api.model.Client.ProjectDetails;

public class Main {
    public static void main(final String[] args) throws Exception {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");

        DslPlatformProxy dpp = new DslPlatformProxy("username", "password");

        testCreateTestProjectByName(dpp);
        testAll(dpp);
    }

    public static void testAll(final DslPlatformProxy dpp) throws Exception {
        final String testProjectName = makeName();
        dpp.doCreate(testProjectName);
        final ProjectDetails pd = dpp.getProjectByName(testProjectName);

        dpp.doClean(pd.getProjectName());
        dpp.doCloneProject(pd.getProjectName());
        dpp.doCreateExternalProject("noname", "noname", "noname", new DatabaseConnection());
        dpp.doDeleteProject(pd.getProjectName());
        dpp.doThatFirstThing();
    }

    public static void testCreateTestProjectByName(final DslPlatformProxy dpp) throws Exception {
        final String testProjectName = makeName();
        dpp.doCreate(testProjectName);
        final ProjectDetails pd = dpp.getProjectByName(testProjectName);

        System.out.println("Name: " +  pd.getProjectName());
        System.out.println("Id: " + pd.getID());
        System.out.println("Created at: " + pd.getCreatedAt());
        dpp.shutdown();
    }

    private static String makeName() {
        return "BlackHawkDown - " + new java.util.Random().nextInt(1000);
    }
}

*/
