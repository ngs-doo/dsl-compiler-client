package com.dslplatform.mojo;

import com.dslplatform.compiler.client.parameters.TempPath;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.plugin.testing.stubs.MavenProjectStub;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import ru.yandex.qatools.embed.service.PostgresEmbeddedService;

import java.io.File;
import java.sql.*;

@RunWith(JUnit4.class)
public class DslPlatformMojoIntegrationTest extends AbstractMojoTestCase {

	private static PostgresEmbeddedService postgres;
	private static Connection conn;

	@BeforeClass
	public static void beforeClass() throws Exception {
		postgres = new PostgresEmbeddedService("localhost", 5429, "test_user", "test_pass", "dsl_platform_mojo_test_db", "target/db", true, 5000);
		postgres.start();
		conn = DriverManager.getConnection("jdbc:postgresql://localhost:5429/dsl_platform_mojo_test_db?user=test_user&password=test_pass");
	}

	@Before
	public void before() throws Exception {
		super.setUp();
	}

	@After
	public void after() throws Exception {
		super.tearDown();
	}

	@AfterClass
	public static void afterClass() throws Exception {
		if (conn != null) conn.close();
		if (postgres != null) postgres.stop();
	}

	@Test
	public void testApplyMigration() throws Exception {
		File pom = getTestFile("src/test/resources/properties-pom.xml");
		assertNotNull(pom);
		assertTrue(pom.exists());

		DslPlatformMojo mojo = (DslPlatformMojo) lookupMojo(DslPlatformMojo.GOAL, pom);
		assertNotNull(mojo);

		mojo.execute();

		assertSchemaExists(conn, "DslPlatformMojoTestModule");
		assertTableExists(conn, "DslPlatformMojoTestModule", "DslPlatformMojoTestAggregate");
		assertColumnExists(conn, "DslPlatformMojoTestModule", "DslPlatformMojoTestAggregate", "ID");
		assertColumnExists(conn, "DslPlatformMojoTestModule", "DslPlatformMojoTestAggregate", "someString");
		assertColumnExists(conn, "DslPlatformMojoTestModule", "DslPlatformMojoTestAggregate", "optionalString");
		assertColumnExists(conn, "DslPlatformMojoTestModule", "DslPlatformMojoTestAggregate", "bodyString");
		assertColumnExists(conn, "DslPlatformMojoTestModule", "DslPlatformMojoTestAggregate", "value");
	}

	@Test
	public void testGenerateCode() throws Exception {
		File pom = getTestFile("src/test/resources/properties-pom.xml");
		assertNotNull(pom);
		assertTrue(pom.exists());

		DslPlatformMojo mojo = (DslPlatformMojo) lookupMojo(DslPlatformMojo.GOAL, pom);
		assertNotNull(mojo);
		mojo.setProject(new MavenProjectStub());
		mojo.execute();

		File tempPath = TempPath.getTempProjectPath(mojo.getContext());
		String sourcesPath = tempPath.getAbsolutePath() + "/JAVA_POJO";
		TestUtils.assertDir(sourcesPath);
		TestUtils.assertDir(sourcesPath + "/DslPlatformMojoTestModule");
		TestUtils.assertFile(sourcesPath + "/DslPlatformMojoTestModule/Guards.java");
		TestUtils.assertFile(sourcesPath + "/DslPlatformMojoTestModule/DslPlatformMojoTestAggregate.java");
	}

	private static void assertSchemaExists(Connection conn, String schemaName) throws SQLException {
		Statement assertStatement = conn.createStatement();
		assertStatement.execute("SELECT EXISTS(SELECT schema_name FROM information_schema.schemata WHERE schema_name = '" + schemaName + "');");
		ResultSet rs = assertStatement.getResultSet();
		assertTrue(rs.next());
		assertEquals("t", rs.getString(1));

	}

	private static void assertTableExists(Connection conn, String schemaName, String tableName) throws SQLException {
		Statement assertStatement = conn.createStatement();
		assertTrue(assertStatement.execute("SELECT EXISTS(SELECT * FROM information_schema.tables WHERE table_schema = '" + schemaName + "' AND table_name = '" + tableName + "');"));
		ResultSet rs = assertStatement.getResultSet();
		assertTrue(rs.next());
		assertEquals("t", rs.getString(1));
	}

	private static void assertColumnExists(Connection conn, String schemaName, String tableName, String columnName) throws SQLException {
		Statement assertStatement = conn.createStatement();
		assertTrue(assertStatement.execute("SELECT EXISTS(SELECT * FROM information_schema.columns WHERE table_schema = '" + schemaName + "' AND table_name = '" + tableName + "' AND column_name = '" + columnName + "');"));
		ResultSet rs = assertStatement.getResultSet();
		assertTrue(rs.next());
		assertEquals("t", rs.getString(1));
	}
}