package com.dslplatform.compiler.client.parameters;

import org.junit.*;

import static com.dslplatform.compiler.client.parameters.DbConnection.extractPostgresVersion;
import static org.junit.Assert.assertEquals;

public class DbConnectionTest {
	@Test
	public void testPostgresStrings() {
		assertEquals("9.3", extractPostgresVersion("PostgreSQL 9.3.0 on x86_64-unknown-linux-gnu, compiled by gcc (Debian 4.4.5-8) 4.4.5, 64-bit", null));
		assertEquals("9.3", extractPostgresVersion("PostgreSQL 9.3.1 on x86_64-unknown-linux-gnu, compiled by gcc (Debian 4.7.2-5) 4.7.2, 64-bit", null));
		assertEquals("9.4", extractPostgresVersion("PostgreSQL 9.4beta2, compiled by Visual C++ build 1800, 64-bit", null));
	}
}
