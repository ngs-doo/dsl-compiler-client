package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.Context;
import com.dslplatform.compiler.client.ExitException;
import com.dslplatform.compiler.client.TestUtils;
import org.junit.Assert;
import org.junit.Test;

import static com.dslplatform.compiler.client.parameters.PostgresConnection.extractPostgresVersion;
import static org.junit.Assert.*;

public class PostgresConnectionTest {
	@Test
	public void testPostgresStrings() {
		assertEquals("9.3", extractPostgresVersion("PostgreSQL 9.3.5 on x86_64-unknown-linux-gnu, compiled by gcc (Debian 4.7.2-5) 4.7.2, 64-bit", null));
		assertEquals("9.3", extractPostgresVersion("PostgreSQL 9.3.0 on x86_64-unknown-linux-gnu, compiled by gcc (Debian 4.4.5-8) 4.4.5, 64-bit", null));
		assertEquals("9.3", extractPostgresVersion("PostgreSQL 9.3.1 on x86_64-unknown-linux-gnu, compiled by gcc (Debian 4.7.2-5) 4.7.2, 64-bit", null));
		assertEquals("9.4", extractPostgresVersion("PostgreSQL 9.4beta2, compiled by Visual C++ build 1800, 64-bit", null));
	}

	@Test
	public void testMigrationFile() throws ExitException {
		Context ctx = new Context();
		ctx.cache("db-version:postgres", "9.5");
		String content = TestUtils.fileContent("/postgres-migration-example1.sql");
		DatabaseInfo dbInfo = PostgresConnection.extractDatabaseInfoFromMigration(ctx, content);
		Assert.assertEquals("9.5", dbInfo.dbVersion);
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
