package com.dslplatform.compiler.client.network;

import com.dslplatform.compiler.client.Either;
import com.dslplatform.compiler.client.InputParameter;
import com.dslplatform.compiler.client.json.JsonValue;
import com.dslplatform.compiler.client.parameters.Password;
import com.dslplatform.compiler.client.parameters.Username;
import sun.misc.BASE64Encoder;
import sun.org.mozilla.javascript.internal.json.JsonParser;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.net.URL;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Map;

public class DslServer {
	private static final String REMOTE_URL = "https://compiler.dsl-platform.com:8443/platform/";

	private static final SSLSocketFactory sslSocketFactory;

	private static SSLSocketFactory createSSLSocketFactory() throws Exception {
		final KeyStore truststore = KeyStore.getInstance("jks");
		truststore.load(DslServer.class.getResourceAsStream("/startssl-ca.jks"), "startssl-ca".toCharArray());
		final TrustManagerFactory tMF = TrustManagerFactory.getInstance("PKIX");
		tMF.init(truststore);
		final SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(null, tMF.getTrustManagers(), new SecureRandom());
		return sslContext.getSocketFactory();
	}

	static {
		try {
			sslSocketFactory = createSSLSocketFactory();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static String readJsonOrText(InputStream stream) throws IOException {
		final String result = read(stream);
		if (result.startsWith("\"") && result.endsWith("\"")) {
			return JsonValue.readFrom(result).asString();
		}
		return result;
	}

	private static String read(InputStream stream) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		StringBuilder sb = new StringBuilder();
		char[] buffer = new char[8192];
		int len;
		while ((len = reader.read(buffer)) != -1) {
			sb.append(buffer, 0, len);
		}
		reader.close();
		return sb.toString();
	}

	private static Either<HttpsURLConnection> setupConnection(
			final String address,
			final Map<InputParameter, String> parameters,
			final boolean sendJson,
			final boolean getJson) {
		final HttpsURLConnection conn;
		try {
			final URL url = new URL(REMOTE_URL + address);
			conn = (HttpsURLConnection) url.openConnection();
		} catch (Exception ex) {
			return Either.fail(ex.getMessage());
		}
		conn.setSSLSocketFactory(sslSocketFactory);
		final Either<String> username = Username.getOrLoad(parameters);
		if (!username.isSuccess()) {
			return Either.fail(username.whyNot());
		}
		final String password = Password.getOrLoad(parameters);
		final BASE64Encoder encoder = new BASE64Encoder();
		conn.setConnectTimeout(30000);
		conn.setReadTimeout(60000);
		try {
			final String base64Login = encoder.encode((username.get() + ":" + password).getBytes("UTF-8"));
			conn.addRequestProperty("Authorization", "Basic " + base64Login);
		} catch (UnsupportedEncodingException ex) {
			return Either.fail(ex.getMessage());
		}
		if (sendJson) {
			conn.addRequestProperty("Content-type", "application/json");
		}
		if (getJson) {
			conn.addRequestProperty("Accept", "application/json");
		}
		return Either.success(conn);
	}

	private static boolean tryRestart(
			final HttpsURLConnection conn,
			final Map<InputParameter, String> parameters) throws IOException {
		System.out.println("Authorization failed.");
		System.out.println(readJsonOrText(conn.getErrorStream()));
		System.out.print("Retry (y/N): ");
		final String value = System.console().readLine();
		if (value.trim().equalsIgnoreCase("Y")) {
			Username.retryInput(parameters);
			Password.retryInput(parameters);
			return true;
		}
		return false;
	}

	public static Either<String> get(final String address, final Map<InputParameter, String> parameters) {
		Either<HttpsURLConnection> tryConn = setupConnection (address, parameters, false, true);
		if (!tryConn.isSuccess()) {
			return Either.fail(tryConn.whyNot());
		}
		HttpsURLConnection conn = tryConn.get();
		try {
			return Either.success(read(conn.getInputStream()));
		} catch (Exception ex) {
			try {
				if (conn.getResponseCode() == 403 && tryRestart(conn, parameters)) {
					return get(address, parameters);
				}
				final InputStream error = conn.getErrorStream();
				if (error != null) {
					return Either.fail(readJsonOrText(error));
				}
			} catch (Exception e) {
				return Either.fail(e.getMessage());
			}
			return Either.fail(ex.getMessage());
		}
	}

	public static Either<String> put(final String address, final Map<InputParameter, String> parameters, String argument) {
		return send(address, "PUT", parameters, argument);
	}

	public static Either<String> post(final String address, final Map<InputParameter, String> parameters, String argument) {
		return send(address, "POST", parameters, argument);
	}

	private static Either<String> send(
			final String address,
			final String method,
			final Map<InputParameter, String> parameters,
			String argument) {
		Either<HttpsURLConnection> tryConn = setupConnection (address, parameters, true, true);
		if (!tryConn.isSuccess()) {
			return Either.fail(tryConn.whyNot());
		}
		HttpsURLConnection conn = tryConn.get();
		try {
			conn.setDoOutput(true);
			conn.setRequestMethod(method);
			final OutputStream os = conn.getOutputStream();
			os.write(argument.getBytes("UTF-8"));
			os.close();
			return Either.success(read(conn.getInputStream()));
		} catch (Exception ex) {
			try {
				if (conn.getResponseCode() == 403 && tryRestart(conn, parameters)) {
					return send(address, method, parameters, argument);
				}
				final InputStream error = conn.getErrorStream();
				if (error != null) {
					return Either.fail(readJsonOrText(error));
				}
			} catch (Exception e) {
				return Either.fail(e.getMessage());
			}
			return Either.fail(ex.getMessage());
		}
	}
}
