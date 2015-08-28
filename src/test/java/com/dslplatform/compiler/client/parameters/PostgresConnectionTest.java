package com.dslplatform.compiler.client.parameters;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.dslplatform.compiler.client.parameters.PostgresConnection.extractPostgresVersion;
import static org.junit.Assert.assertEquals;

public class PostgresConnectionTest {
	@Test
	public void testPostgresStrings() {
		assertEquals("9.3", extractPostgresVersion("PostgreSQL 9.3.5 on x86_64-unknown-linux-gnu, compiled by gcc (Debian 4.7.2-5) 4.7.2, 64-bit", null));
		assertEquals("9.3", extractPostgresVersion("PostgreSQL 9.3.0 on x86_64-unknown-linux-gnu, compiled by gcc (Debian 4.4.5-8) 4.4.5, 64-bit", null));
		assertEquals("9.3", extractPostgresVersion("PostgreSQL 9.3.1 on x86_64-unknown-linux-gnu, compiled by gcc (Debian 4.7.2-5) 4.7.2, 64-bit", null));
		assertEquals("9.4", extractPostgresVersion("PostgreSQL 9.4beta2, compiled by Visual C++ build 1800, 64-bit", null));
	}

	@Test
	public void testSplitConnectionString() {
		assertEquals(
				PostgresConnection.splitConnectionString("localhost/Database"),
				new HashMap<String, String>() {{
					put(null, "localhost/Database");
				}});

		assertEquals(
				PostgresConnection.splitConnectionString("my.server.com:5432/MyDatabase?protocolVersion=1&user=user&password=password"),
				new HashMap<String, String>() {{
					put(null, "my.server.com:5432/MyDatabase");
					put("protocolVersion", "1");
					put("user", "user");
					put("password", "password");
				}});

		assertEquals(
				PostgresConnection.splitConnectionString("server:5432/DB?user=migration&password=food=good&ssl=true&protocolVersion=2"),
				new HashMap<String, String>() {{
					put(null, "server:5432/DB");
					put("user", "migration");
					put("password", "food=good");
					put("ssl", "true");
					put("protocolVersion", "2");
				}});

		assertEquals(
				PostgresConnection.splitConnectionString("my.server.com:5432/MyDatabase?empty1=&empty2=&empty3"),
				new HashMap<String, String>() {{
					put(null, "my.server.com:5432/MyDatabase");
					put("empty1", "");   // equals
					put("empty2", "");   // equals
					put("empty3", null); // no equals
				}});
	}

	@Test
	public void testJoinConnectionParams() {
		final Map<String, String> p1 = PostgresConnection.splitConnectionString("localhost/Database");
		p1.put("protocolVersion", "2");
		assertEquals("localhost/Database?protocolVersion=2", PostgresConnection.joinConnectionParams(p1));

		final Map<String, String> p2 = PostgresConnection.splitConnectionString("my.server.com:5432/MyDatabase?protocolVersion=1&user=user&password=password");
		p2.put("protocolVersion", "2");
		assertEquals("my.server.com:5432/MyDatabase?protocolVersion=2&user=user&password=password", PostgresConnection.joinConnectionParams(p2));

		final Map<String, String> p3 = PostgresConnection.splitConnectionString("server:5432/DB?user=migration&password=food=good&ssl=true&protocolVersion=2");
		p3.put("protocolVersion", "2");
		assertEquals("server:5432/DB?user=migration&password=food=good&ssl=true&protocolVersion=2", PostgresConnection.joinConnectionParams(p3));

		final Map<String, String> p4 = PostgresConnection.splitConnectionString("my.server.com:5432/MyDatabase?empty1=&empty2=&empty3");
		p4.put("protocolVersion", "2");
		assertEquals("my.server.com:5432/MyDatabase?empty1=&empty2=&empty3&protocolVersion=2", PostgresConnection.joinConnectionParams(p4));
	}
}
