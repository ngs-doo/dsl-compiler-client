package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.Context;
import com.dslplatform.compiler.client.ExitException;

import java.util.LinkedHashMap;
import java.util.Map;

class DatabaseInfo {
	public final String database;
	public final String compilerVersion;
	public final String dbVersion;
	public final Map<String, String> dsl;

	public DatabaseInfo(final String database, final String compilerVersion, final String dbVersion, final Map<String, String> dsl) {
		this.database = database;
		this.compilerVersion = compilerVersion;
		this.dbVersion = dbVersion;
		this.dsl = dsl;
	}

	private static String unescape(final String element) {
		return element.replace("\\\"", "\"").replace("\\\\", "\\");
	}

	public static Map<String, String> convertToMap(final String dsls, final Context context) throws ExitException {
		final Map<String, String> tuples = new LinkedHashMap<String, String>();
		if (dsls == null || dsls.length() == 0) {
			return tuples;
		}
		final int endLength = dsls.length() - 1;
		if (dsls.charAt(0) != '"' || dsls.charAt(endLength) != '"') {
			context.error("Invalid DSL found in database. Unable to parse it as map: " + dsls);
			throw new ExitException();
		}
		final String[] pairs = dsls.substring(1, endLength).split("\", ?\"", -1);
		for (final String pair : pairs) {
			final String[] kv = pair.split("\"=>\"", -1);
			if (kv.length != 2) {
				context.error("Invalid DSL found in database. Unable to parse it as map: " + dsls);
				throw new ExitException();
			}
			tuples.put(unescape(kv[0]), unescape(kv[1]));
		}
		return tuples;
	}
}