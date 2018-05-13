package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.Context;
import com.dslplatform.compiler.client.ExitException;
import com.dslplatform.compiler.client.TestUtils;
import org.junit.Assert;
import org.junit.Test;

import static com.dslplatform.compiler.client.parameters.PostgresConnection.extractPostgresVersion;
import static org.junit.Assert.assertEquals;

public class OracleConnectionTest {
	@Test
	public void testSimpleMigrationFile() throws ExitException {
		Context ctx = new Context();
		ctx.cache("db-version:oracle", "10.2");
		String content = TestUtils.fileContent("/oracle-migration-example1.sql");
		DatabaseInfo dbInfo = OracleConnection.extractDatabaseInfoFromMigration(ctx, content);
		Assert.assertEquals("10.2", dbInfo.dbVersion);
		Assert.assertEquals("2.1.0.14620", dbInfo.compilerVersion);
		Assert.assertEquals(3, dbInfo.dsl.size());
		Assert.assertEquals("module spec\r\n" +
				"{\r\n" +
				"  aggregate Fact(date, account) {\r\n" +
				"    Date  date;\r\n" +
				"    Long  account { index; }\r\n" +
				"\r\n" +
				"    specification ByAccounts 'it => ids.Contains(it.accountID)' {\r\n" +
				"      Long[]  ids;\r\n" +
				"    }\r\n" +
				"    persistence { specification delete enabled; }\r\n" +
				"  }\r\n", dbInfo.dsl.get("test/quotes.dsl"));
		Assert.assertEquals("module escapes\r\n" +
				"{\r\n" +
				"  aggregate Cheque(number, bank) {\r\n" +
				"    String  number;\r\n" +
				"    String  bank { default 'it => \"Test me\"'; }\r\n" +
				"    Bool    cancelled; // Only an \"Test me\" can be used\r\n" +
				"  }\r\n" +
				"}\r\n", dbInfo.dsl.get("test/escapes.dsl"));
	}

	@Test
	public void testComplexMigrationFile() throws ExitException {
		Context ctx = new Context();
		ctx.cache("db-version:oracle", "10.2");
		String content = TestUtils.fileContent("/oracle-migration-example2.sql");
		DatabaseInfo dbInfo = OracleConnection.extractDatabaseInfoFromMigration(ctx, content);
		Assert.assertEquals("10.2", dbInfo.dbVersion);
		Assert.assertEquals("2.1.0.14620", dbInfo.compilerVersion);
		Assert.assertEquals(3, dbInfo.dsl.size());
		Assert.assertEquals("module spec\r\n" +
				"{\r\n" +
				"  aggregate Fact(date, account) {\r\n" +
				"    Date  date;\r\n" +
				"    Long  account { index; }\r\n" +
				"\r\n" +
				"    specification ByAccounts 'it => ids.Contains(it.accountID)' {\r\n" +
				"      Long[]  ids;\r\n" +
				"    }\r\n" +
				"    persistence { specification delete enabled; }\r\n" +
				"  }\r\n", dbInfo.dsl.get("test/quotes.dsl"));
		Assert.assertEquals("module escapes\r\n" +
				"{\r\n" +
				"  aggregate Cheque(number, bank) {\r\n" +
				"    String  number;\r\n" +
				"    String  bank { default 'it => \"Test me\"'; }\r\n" +
				"    Bool    cancelled; // Only an \"Test me\" can be used\r\n" +
				"  }\r\n" +
				"}\r\n", dbInfo.dsl.get("test/escapes.dsl"));
	}
}
