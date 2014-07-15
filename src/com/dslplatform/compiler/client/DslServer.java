package com.dslplatform.compiler.client;

import com.dslplatform.compiler.client.json.JsonValue;
import com.dslplatform.compiler.client.parameters.Password;
import com.dslplatform.compiler.client.parameters.Prompt;
import com.dslplatform.compiler.client.parameters.Username;
import org.w3c.dom.Document;
import sun.misc.BASE64Encoder;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
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

	private static String readResponseError(final HttpsURLConnection conn) throws IOException {
		if (conn.getContentType() != null && conn.getContentType().startsWith("application/xml")) {
			try {
				synchronized (sslSocketFactory) {
					final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
					final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
					final Document doc = dBuilder.parse(conn.getErrorStream());
					final String error = doc.getDocumentElement().getTextContent();
					return error != null ? error : "UNKNOWN ERROR";
				}
			} catch (Exception ex) {
				return "INTERNAL ERROR: Error reading xml response";
			}
		}
		final String result = Utils.read(conn.getErrorStream());
		if ("application/json".equals(conn.getContentType()) && result.length() > 0) {
			return JsonValue.readFrom(result).asString();
		}
		return result;
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
		System.out.println(readResponseError(conn));
		if (!Prompt.canUsePrompt()) {
			System.exit(0);
		}
		System.out.print("Retry (y/N): ");
		final String value = System.console().readLine();
		if (!"y".equalsIgnoreCase(value)) {
			System.exit(0);
		}
		System.out.println("Retrying...");
		Username.retryInput(parameters);
		Password.retryInput(parameters);
		return true;
	}

	public static Either<String> get(final String address, final Map<InputParameter, String> parameters) {
		Either<HttpsURLConnection> tryConn = setupConnection(address, parameters, false, true);
		if (!tryConn.isSuccess()) {
			return Either.fail(tryConn.whyNot());
		}
		HttpsURLConnection conn = tryConn.get();
		try {
			return Either.success(Utils.read(conn.getInputStream()));
		} catch (Exception ex) {
			try {
				if (conn.getResponseCode() == 403 && tryRestart(conn, parameters)) {
					return get(address, parameters);
				}
				if (conn.getErrorStream() != null) {
					return Either.fail(readResponseError(conn));
				}
			} catch (Exception e) {
				return Either.fail(e.getMessage());
			}
			return Either.fail(ex.getMessage());
		}
	}

	public static Either<String> put(final String address, final Map<InputParameter, String> parameters, JsonValue json) {
		return send(address, "PUT", parameters, json.toString());
	}

	/*public static Either<String> post(final String address, final Map<InputParameter, String> parameters, JsonValue json) {
		return send(address, "POST", parameters, json.toString());
	}*/

	private static Either<String> send(
			final String address,
			final String method,
			final Map<InputParameter, String> parameters,
			final String argument) {
		Either<HttpsURLConnection> tryConn = setupConnection(address, parameters, true, true);
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
			return Either.success(Utils.read(conn.getInputStream()));
		} catch (Exception ex) {
			try {
				if (conn.getResponseCode() == 403 && tryRestart(conn, parameters)) {
					return send(address, method, parameters, argument);
				}
				if (conn.getErrorStream() != null) {
					return Either.fail(readResponseError(conn));
				}
			} catch (Exception e) {
				return Either.fail(e.getMessage());
			}
			return Either.fail(ex.getMessage());
		}
	}
}
