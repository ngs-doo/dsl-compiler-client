package com.dslplatform.compiler.client;

import com.dslplatform.compiler.client.json.JsonValue;
import com.dslplatform.compiler.client.parameters.Password;
import com.dslplatform.compiler.client.parameters.Username;
import org.w3c.dom.Document;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;

public class DslServer {
	private static final String REMOTE_URL = "https://compiler.dsl-platform.com:8443/platform/";

	private static String readResponseError(final HttpURLConnection conn) throws IOException {
		if (conn.getContentType() != null && conn.getContentType().startsWith("application/xml")) {
			final Either<Document> xml = Utils.readXml(conn.getErrorStream());
			if (!xml.isSuccess()) {
				return "INTERNAL ERROR: Error reading xml response\n" + xml.explainError();
			}
			final String error = xml.get().getDocumentElement().getTextContent();
			return error != null ? error : "UNKNOWN ERROR";
		}
		final String result = Utils.read(conn.getErrorStream());
		if ("application/json".equals(conn.getContentType()) && result.length() > 0) {
			return JsonValue.readFrom(result).asString();
		}
		return result;
	}

	private static Either<HttpURLConnection> setupConnection(
			final String address,
			final Context context,
			final boolean sendJson,
			final boolean getJson) throws ExitException {
		final HttpURLConnection conn;
		try {
			final URL url = new URL(REMOTE_URL + address);
			context.log("Calling: " + url.toString());
			conn = (HttpURLConnection) url.openConnection();
		} catch (IOException ex) {
			return Either.fail(ex);
		}
		final Either<String> username = Username.getOrLoad(context);
		if (!username.isSuccess()) {
			return Either.fail(username.whyNot());
		}
		final String password = Password.getOrLoad(context);
		conn.setConnectTimeout(10000);
		conn.setReadTimeout(60000);
		try {
			final String base64Login = DatatypeConverter.printBase64Binary((username.get() + ":" + password).getBytes("UTF-8"));
			conn.addRequestProperty("Authorization", "Basic " + base64Login);
		} catch (UnsupportedEncodingException ex) {
			return Either.fail(ex);
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
			final HttpURLConnection conn,
			final Context context) throws IOException, ExitException {
		context.error("Authorization failed.");
		context.error(readResponseError(conn));
		if (!context.canInteract()) {
			throw new ExitException();
		}
		final String value = context.ask("Retry (y/N):");
		if (!"y".equalsIgnoreCase(value)) {
			throw new ExitException();
		}
		context.show("Retrying...");
		Username.retryInput(context);
		Password.retryInput(context);
		return true;
	}

	public static Either<String> put(final String address, final Context context, JsonValue json) throws ExitException {
		return send(address, "PUT", context, json.toString());
	}

	private static Either<String> send(
			final String address,
			final String method,
			final Context context,
			final String argument) throws ExitException {
		Either<HttpURLConnection> tryConn = setupConnection(address, context, true, true);
		if (!tryConn.isSuccess()) {
			return Either.fail(tryConn.whyNot());
		}
		HttpURLConnection conn = tryConn.get();
		try {
			conn.setDoOutput(true);
			conn.setRequestMethod(method);
			final OutputStream os = conn.getOutputStream();
			os.write(argument.getBytes("UTF-8"));
			os.close();
			return Either.success(Utils.read(conn.getInputStream()));
		} catch (UnknownHostException ex) {
			return Either.fail("Error connecting to compiler.dsl-platform.com\nCheck if Internet connection is down.", ex);
		} catch (IOException ex) {
			try {
				if (conn.getResponseCode() == 403 && tryRestart(conn, context)) {
					return send(address, method, context, argument);
				}
				if (conn.getErrorStream() != null) {
					return Either.fail(readResponseError(conn));
				}
			} catch (IOException e) {
				return Either.fail(e);
			}
			return Either.fail(ex);
		}
	}

	public static void downloadAndUnpack(final Context context, final String file, final File path) throws IOException {
		final URL server = new URL(REMOTE_URL + "download/" + file + ".zip");
		context.log("Downloading " + file + ".zip ...");
		Utils.unpackZip(context, path, server);
	}
}
