package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.Context;
import com.dslplatform.compiler.client.ExitException;
import org.postgresql.util.Base64;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.Map;

public class DatabaseInfo {
	public final String database;
	public final String compilerVersion;
	public final String dbVersion;
	public final Map<String, String> dsl;
	public final String previousHash;

	private static final Charset UTF8 = Charset.forName("UTF-8");

	DatabaseInfo(final String database, final String compilerVersion, final String dbVersion, final Map<String, String> dsl, final String previousHash) {
		this.database = database;
		this.compilerVersion = compilerVersion;
		this.dbVersion = dbVersion;
		this.dsl = dsl;
		this.previousHash = previousHash;
	}

	public String currentHash() {
		try {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			for (final String v : dsl.values()) {
				stream.write(v.getBytes(UTF8));
			}
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			byte[] hash = md5.digest(stream.toByteArray());
			return Base64.encodeBytes(hash);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private static String unescape(final String element) {
		return element.replace("\\\"", "\"").replace("\\\\", "\\");
	}

	static Map<String, String> convertToMap(final String dsls, final Context context) throws ExitException {
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
