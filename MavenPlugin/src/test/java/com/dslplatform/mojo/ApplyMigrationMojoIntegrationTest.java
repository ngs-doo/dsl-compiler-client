package com.dslplatform.mojo;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import ru.yandex.qatools.embed.service.PostgresEmbeddedService;

import java.io.File;
import java.sql.*;

@RunWith(JUnit4.class)
public class ApplyMigrationMojoIntegrationTest extends AbstractMojoTestCase {

	private static PostgresEmbeddedService postgres;
	private static Connection conn;

	@BeforeClass
	public static void beforeClass() throws Exception {
		postgres = new PostgresEmbeddedService("localhost", 5429, "test_user", "test_pass", "test_db", "target/db", true, 5000);
		postgres.start();
		conn = DriverManager.getConnection("jdbc:postgresql://localhost:5429/test_db?user=test_user&password=test_pass");
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
		File pom = getTestFile("src/test/resources/sql-migration-pom.xml");
		assertNotNull(pom);
		assertTrue(pom.exists());

		SqlMigrationMojo mojo = (SqlMigrationMojo) lookupMojo(SqlMigrationMojo.GOAL, pom);
		assertNotNull(mojo);

		mojo.execute();

		assertSchemaExists(conn, "MojoTestModule");
		assertTableExists(conn, "MojoTestModule", "MojoTestAggregate");
		assertColumnExists(conn, "MojoTestModule", "MojoTestAggregate", "ID");
		assertColumnExists(conn, "MojoTestModule", "MojoTestAggregate", "someString");
		assertColumnExists(conn, "MojoTestModule", "MojoTestAggregate", "optionalString");
		assertColumnExists(conn, "MojoTestModule", "MojoTestAggregate", "bodyString");
		assertColumnExists(conn, "MojoTestModule", "MojoTestAggregate", "value");
	}

	private static void assertSchemaExists(Connection conn, String schemaName) throws SQLException {
		//System.out.println("Asserting schema exists: " + schemaName);
		Statement assertStatement = conn.createStatement();
		assertStatement.execute("SELECT EXISTS(SELECT schema_name FROM information_schema.schemata WHERE schema_name = '" + schemaName + "');");
		ResultSet rs = assertStatement.getResultSet();
		assertTrue(rs.next());
		assertEquals("t", rs.getString(1));

	}

	private static void assertTableExists(Connection conn, String schemaName, String tableName) throws SQLException {
		//System.out.println("Asserting table exists: " + schemaName + ", " + tableName);
		Statement assertStatement = conn.createStatement();
		assertTrue(assertStatement.execute("SELECT EXISTS(SELECT * FROM information_schema.tables WHERE table_schema = '" + schemaName + "' AND table_name = '" + tableName + "');"));
		ResultSet rs = assertStatement.getResultSet();
		assertTrue(rs.next());
		assertEquals("t", rs.getString(1));
	}

	private static void assertColumnExists(Connection conn, String schemaName, String tableName, String columnName) throws SQLException {
		//System.out.println("Asserting column exists: " + schemaName + ", " + tableName + ", " + columnName);
		Statement assertStatement = conn.createStatement();
		assertTrue(assertStatement.execute("SELECT EXISTS(SELECT * FROM information_schema.columns WHERE table_schema = '" + schemaName + "' AND table_name = '" + tableName + "' AND column_name = '" + columnName + "');"));
		ResultSet rs = assertStatement.getResultSet();
		assertTrue(rs.next());
		assertEquals("t", rs.getString(1));
	}
}