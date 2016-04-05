package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.*;
import com.dslplatform.compiler.client.json.DslJson;
import org.w3c.dom.*;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.*;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;

public enum DslCompiler implements CompileParameter, ParameterParser {
	INSTANCE;

	@Override
	public String getAlias() {
		return "compiler";
	}

	@Override
	public String getUsage() {
		return "path or IP port";
	}

	private final static Charset UTF_8 = Charset.forName("UTF-8");

	public static final String DSL_COMPILER_SOCKET = "dsl-compiler-socket";

	public static Map<String, String> compile(
			final Context context,
			final String target,
			final List<Settings.Option> settings,
			final String namespace,
			final String version,
			final List<File> dsls) throws ExitException {
		final Map<String, String> files = new HashMap<String, String>();
		final List<String> arguments = new ArrayList<String>();
		arguments.add("target=" + target);
		if (namespace != null && namespace.length() > 0) {
			arguments.add("namespace=" + namespace);
		}
		if (version != null && version.length() > 0) {
			arguments.add("version=" + version);
		}
		if (settings != null) {
			for (final Settings.Option o : settings) {
				arguments.add("settings=" + o);
			}
		}
		for (final File f : dsls) {
			arguments.add("dsl=" + f.getAbsolutePath());
		}
		context.log("Compiling DSL to " + target + "...");
		final Either<byte[]> response = runCompiler(context, arguments);
		if (!response.isSuccess()) {
			context.error(response.whyNot());
			throw new ExitException();
		}
		final Either<Document> xml = Utils.readXml(new ByteArrayInputStream(response.get()));
		if (!xml.isSuccess()) {
			context.error(new String(response.get(), UTF_8));
			throw new ExitException();
		}
		final NodeList nodes = xml.get().getDocumentElement().getChildNodes();
		try {
			for (int i = 0; i < nodes.getLength(); i++) {
				final Element item = (Element) nodes.item(i);
				final Node key = item.getElementsByTagName("Key").item(0);
				final Node value = item.getElementsByTagName("Value").item(0);
				files.put(key.getTextContent(), value.getTextContent());
			}
		} catch (final Exception ex) {
			context.error("Invalid xml found");
			context.error(new String(response.get(), UTF_8));
			throw new ExitException();
		}
		context.notify("SOURCES", files);
		return files;
	}

	private static Either<byte[]> runCompiler(Context context, List<String> arguments) throws ExitException {
		final Socket socket = context.load(DSL_COMPILER_SOCKET);
		final File compiler = new File(context.get(DslCompiler.INSTANCE));
		arguments.add("path=" + System.getProperty("user.dir"));
		context.notify("DSL", arguments);
		return socket != null
				? runCompilerSocket(context, socket, arguments)
				: runCompilerFile(context, compiler, arguments);
	}

	public static class ParseError {
		public final int line;
		public final int column;
		public final String error;

		ParseError(Map<String, Object> map) {
			this.line = map.containsKey("Line") ? ((Number) map.get("Line")).intValue() : 0;
			this.column = map.containsKey("Column") ? ((Number) map.get("Column")).intValue() : 0;
			this.error = (String) map.get("Error");
		}
	}

	public static class ParseResult {
		public final ParseError error;
		public final List<SyntaxConcept> tokens;

		@SuppressWarnings("unchecked")
		ParseResult(Map<String, Object> map) {
			List<Map<String, Object>> tokenMap = (List<Map<String, Object>>) map.get("Tokens");
			this.tokens = new ArrayList<SyntaxConcept>(tokenMap != null ? tokenMap.size() : 0);
			if (tokenMap != null) {
				for (Map<String, Object> t : tokenMap) {
					tokens.add(new SyntaxConcept(t));
				}
			}
			Object err = map.get("Error");
			this.error = err != null ? new ParseError((Map<String, Object>) err) : null;
		}
	}

	public enum SyntaxType {
		Keyword,
		Identifier,
		StringQuote,
		Expression,
		Type,
		Navigation,
		RuleStart,
		RuleExtension,
		RuleEnd
	}

	public static class SyntaxConcept {
		public final SyntaxType type;
		public final String value;
		public final String script;
		public final int line;
		public final int column;

		SyntaxConcept(Map<String, Object> map) {
			if (map.containsKey("Type")) {
				Object value = map.get("Type");
				if (value instanceof String) {
					this.type = SyntaxType.valueOf((String) value);
				} else {
					this.type = SyntaxType.values()[((Number) value).intValue()];
				}
			} else {
				this.type = SyntaxType.Keyword;
			}
			this.value = map.containsKey("Value") ? (String) map.get("Value") : "";
			this.script = map.containsKey("Script") ? (String) map.get("Script") : "";
			this.line = map.containsKey("Line") ? ((Number) map.get("Line")).intValue() : 0;
			this.column = map.containsKey("Column") ? ((Number) map.get("Column")).intValue() : 0;
		}
	}

	private static class ByteStream extends ByteArrayOutputStream {
		private final byte[] temp = new byte[8192];

		byte[] getBuffer() {
			return buf;
		}
	}

	private static int readInt(final byte[] buf) {
		int ret = 0;
		for (int i = 0; i < 4; i++) {
			ret <<= 8;
			ret |= (int) buf[i] & 0xFF;
		}
		return ret;
	}

	public static Either<Process> startServer(Context context, File compiler, int port) {
		final List<String> arguments = new ArrayList<String>();
		arguments.add(compiler.getAbsolutePath());
		arguments.add("server-mode");
		arguments.add("port=" + port);
		try {
			if (InetAddress.getLocalHost() instanceof Inet4Address) {
				arguments.add("ip=v4");
			}
		} catch (UnknownHostException ignore) {
		}
		try {
			String procId = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
			arguments.add("parent=" + procId);
		} catch (Exception ignore) {
		}
		if (!Utils.isWindows()) {
			final Either<String> mono = Mono.findMono(context);
			if (mono.isSuccess()) {
				arguments.add(0, mono.get());
			} else {
				return Either.fail("Mono is required to run DSL compiler. Mono not detected or specified.");
			}
		}
		final ProcessBuilder pb = new ProcessBuilder(arguments);
		context.put(DslCompiler.INSTANCE, Integer.toString(port));
		try {
			return Either.success(pb.start());
		} catch (IOException e) {
			return Either.fail(e);
		}
	}

	public static Either<ParseResult> parseTokens(
			final Context context,
			final Socket socket,
			final String dsl) throws IOException {
		final byte[] dslUtf8 = dsl.getBytes(UTF_8);
		final String command = "tokens=" + dslUtf8.length + " format=json include-length keep-alive\n";
		try {
			final OutputStream sos = socket.getOutputStream();
			sos.write(command.getBytes(UTF_8));
			sos.write(dslUtf8);
			sos.flush();
			final ByteStream os = getByteStream(context);
			final byte[] buf = os.temp;
			final InputStream is = socket.getInputStream();
			int read = is.read(buf, 0, 4);
			if (read != 4 || buf[0] != 'O') {
				return Either.fail("Invalid response from server.");
			}
			read = is.read(buf, 0, 4);
			if (read != 4) {
				return Either.fail("Invalid response from server. Expecting length.");
			}
			int length = readInt(buf);
			os.reset();
			while (length > 0 && (read = is.read(buf)) > 0) {
				length -= read;
				os.write(buf, 0, read);
			}
			os.flush();
			return Either.success(new ParseResult(DslJson.readMap(os.getBuffer(), os.size())));
		} catch (IOException e) {
			return Either.fail(e.getMessage());
		}
	}

	private static boolean hasWhitespace(final String input) {
		for (int i = 0; i < input.length(); i++) {
			if (Character.isWhitespace(input.charAt(i)))
				return true;
		}
		return false;
	}

	private static Either<byte[]> runCompilerSocket(
			final Context context,
			final Socket socket,
			final List<String> arguments) throws ExitException {
		final StringBuilder sb = new StringBuilder();
		for (String arg : arguments) {
			if (!arg.startsWith("\"") && hasWhitespace(arg)) {
				sb.append('"');
				sb.append(arg);
				sb.append('"');
			} else {
				sb.append(arg);
			}
			sb.append(' ');
		}
		sb.append("include-length keep-alive\n");
		try {
			final OutputStream sos = socket.getOutputStream();
			sos.write(sb.toString().getBytes(UTF_8));
			sos.flush();
			final ByteStream os = getByteStream(context);
			final byte[] buf = os.temp;
			final InputStream is = socket.getInputStream();
			int read = is.read(buf, 0, 4);
			final boolean success = read == 4 && buf[0] == 'O';
			read = is.read(buf, 0, 4);
			if (read != 4) {
				return Either.fail("Invalid response from server. Expecting length.");
			}
			int length = readInt(buf);
			context.log("Response size from DSL compiler: " + length);
			os.reset();
			while (length > 0 && (read = is.read(buf)) > 0) {
				length -= read;
				os.write(buf, 0, read);
			}
			os.flush();
			if (!success) {
				return Either.fail(os.toString("UTF-8"));
			}
			return Either.success(os.toByteArray());
		} catch (IOException e) {
			context.error(e);
			throw new ExitException();
		}
	}

	private static ByteStream getByteStream(Context context) {
		ByteStream os = context.load("dsl-stream");
		if (os == null) {
			os = new ByteStream();
			context.cache("dsl-stream", os);
		}
		return os;
	}

	private static Either<byte[]> runCompilerFile(
			final Context context,
			final File compiler,
			final List<String> arguments) throws ExitException {
		final Either<Utils.CommandResult> result;
		if (Utils.isWindows()) {
			result = Utils.runCommand(context, compiler.getAbsolutePath(), compiler.getParentFile(), arguments);
		} else {
			final Either<String> mono = Mono.findMono(context);
			if (mono.isSuccess()) {
				arguments.add(0, compiler.getAbsolutePath());
				result = Utils.runCommand(context, mono.get(), compiler.getParentFile(), arguments);
			} else {
				context.error("Mono is required to run DSL compiler. Mono not detected or specified.");
				throw new ExitException();
			}
		}
		if (!result.isSuccess()) {
			return Either.fail(result.whyNot());
		}
		if (result.get().exitCode != 0) {
			return Either.fail(result.get().output + result.get().error);
		}
		return Either.success(result.get().output.getBytes(UTF_8));
	}

	public static Either<String> migration(
			final Context context,
			final DatabaseInfo dbInfo,
			final List<File> currentDsls) throws ExitException {
		final List<String> arguments = new ArrayList<String>();
		arguments.add("target=" + dbInfo.database.toLowerCase() + dbInfo.dbVersion);
		if (context.contains(VarraySize.INSTANCE)) {
			arguments.add("varray=" + context.get(VarraySize.INSTANCE));
		}
		if (context.contains(GrantRole.INSTANCE)) {
			arguments.add("role=" + context.get(GrantRole.INSTANCE));
		}
		if (dbInfo.dsl != null && !dbInfo.dsl.isEmpty()) {
			final StringBuilder oldDsl = new StringBuilder();
			for (final String v : dbInfo.dsl.values()) {
				oldDsl.append(v);
			}
			final File previousDsl = new File(TempPath.getTempProjectPath(context), "old.dsl");
			try {
				Utils.saveFile(context, previousDsl, oldDsl.toString());
			} catch (IOException ex) {
				context.error("Unable to save old DSL version for comparison.");
				return Either.fail(ex);
			}
			arguments.add("previous-dsl=" + previousDsl.getAbsolutePath());
			if (dbInfo.compilerVersion != null) {
				arguments.add("previous-compiler=" + dbInfo.compilerVersion);
			}
		}
		for (final File f : currentDsls) {
			arguments.add("dsl=" + f.getAbsolutePath());
		}
		context.log("Creating SQL migration for " + dbInfo.database + " ...");
		final Either<byte[]> result = runCompiler(context, arguments);
		if (!result.isSuccess()) {
			return Either.fail(result.whyNot());
		}
		final String sql = new String(result.get(), UTF_8);
		return Either.success(context.notify("MIGRATION", sql));
	}

	public static Either<Boolean> parse(final Context context, final List<File> dsls) throws ExitException {
		final List<String> arguments = new ArrayList<String>();
		for (final File f : dsls) {
			arguments.add("dsl=" + f.getAbsolutePath());
		}
		context.log("Parsing DSL...");
		final Either<byte[]> response = runCompiler(context, arguments);
		if (!response.isSuccess()) {
			return Either.fail(response.whyNot());
		}
		return Either.success(false);
	}

	@Override
	public Either<Boolean> tryParse(final String name, final String value, final Context context) {
		if ("compiler".equals(name)) {
			context.put(INSTANCE, value);
			return Either.success(true);
		} else {
			return Either.success(false);
		}
	}

	@Override
	public boolean check(final Context context) throws ExitException {
		final String value = context.contains(INSTANCE) ? context.get(INSTANCE) : null;
		final boolean isEmpty = value == null || value.length() == 0;
		if (!isEmpty) {
			int port = -1;
			try {
				port = Integer.parseInt(value);
			} catch (NumberFormatException ignore) {
			}
			if (port > 0) {
				Socket socket;
				try {
					socket = new Socket(InetAddress.getLocalHost(), port);
				} catch (Exception ex) {
					context.log("Unable to open socket on default localhost: " + value);
					try {
						socket = new Socket("::1", port);
					} catch (Exception ex6) {
						context.log("Unable to open socket to port on IPv6 localhost: " + value);
						try {
							socket = new Socket("127.0.0.1", port);
						} catch (Exception ex4) {
							context.error("Unable to open socket to port on localhost: " + value);
							context.error(ex4);
							throw new ExitException();
						}
					}
				}
				if (socket.isConnected()) {
					try {
						socket.setKeepAlive(true);
						socket.setSoTimeout(30000);
					} catch (SocketException ignore) {
					}
					context.cache(DSL_COMPILER_SOCKET, socket);
					return true;
				} else {
					context.error("Unable to connect to specified compiler on localhost:" + value);
					throw new ExitException();
				}
			}
		}
		final File path = new File(isEmpty ? "dsl-compiler.exe" : value);
		if (path.isDirectory()) {
			context.error("Specified compiler path is a directory: " + path.getAbsolutePath());
			throw new ExitException();
		}
		if (!path.exists()) {
			if (!isEmpty) {
				context.error("Specified compiler path not found: " + path.getAbsolutePath());
			}
			final File tempPath = TempPath.getTempRootPath(context);
			final File compiler = new File(tempPath, "dsl-compiler.exe");
			if (compiler.exists() && testCompiler(context, compiler)) {
				if (isEmpty) {
					if (context.contains(Download.INSTANCE)) {
						context.show("Checking for latest compiler version due to download option");
						checkForLatestVersion(context, path, tempPath, compiler);
					}
					context.put(INSTANCE, compiler.getAbsolutePath());
					return true;
				}
				if (context.canInteract()) {
					final String answer =
							context.ask("Compiler found in default location: "
									+ compiler.getAbsolutePath() + ". Do you wish to use it? (y/N)");
					if (answer.toLowerCase().equals("y")) {
						context.put(INSTANCE, compiler.getAbsolutePath());
						return true;
					}
				}
			}
			if (!context.contains(Download.INSTANCE)) {
				if (context.canInteract()) {
					final String answer = context.ask("Do you wish to download compiler from the Internet? (y/N)");
					if (!answer.toLowerCase().equals("y")) throw new ExitException();
				} else throw new ExitException();
			}
			downloadCompiler(context, path, tempPath, compiler);
		} else {
			if (!testCompiler(context, path)) {
				context.error("Specified compiler is invalid: " + path.getAbsolutePath());
				throw new ExitException();
			}
			context.put(INSTANCE, path.getAbsolutePath());
		}
		return true;
	}

	public static void checkForLatestVersion(Context context, File path, File tempPath, File compiler) throws ExitException {
		final Either<Long> lastModified = Utils.lastModified(context, "dsl-compiler");
		if (!lastModified.isSuccess()) {
			context.error(lastModified.whyNot());
		} else {
			if (compiler.lastModified() == lastModified.get()) {
				final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				context.show("dsl-compiler.exe at latest version (" + sdf.format(compiler.lastModified()) + ")");
			} else {
				context.show("Newer version of dsl-compiler.exe found at DSL Platform website.");
				downloadCompiler(context, path, tempPath, compiler);
			}
		}
	}

	private static void downloadCompiler(final Context context, final File path, final File tempPath, final File compiler) throws ExitException {
		final long lastModified;
		try {
			lastModified = Utils.downloadAndUnpack(context, "dsl-compiler", tempPath);
		} catch (final IOException ex) {
			context.error("Error downloading compiler from https://dsl-platform.com");
			context.error(ex);
			throw new ExitException();
		}
		if (!testCompiler(context, compiler)) {
			context.error("Specified compiler is invalid: " + path.getAbsolutePath());
			throw new ExitException();
		}
		if (!compiler.setLastModified(lastModified)) {
			context.error("Unable to set matching last modified date");
		}
		context.put(INSTANCE, compiler.getAbsolutePath());
	}

	private static boolean testCompiler(final Context context, final File path) throws ExitException {
		if (Utils.isWindows()) {
			return Utils.testCommand(context, path.getAbsolutePath(), "DSL Platform");
		} else {
			final Either<String> mono = Mono.findMono(context);
			if (mono.isSuccess()) {
				return Utils.testCommand(context, mono.get(), "DSL Platform", Collections.singletonList(path.getAbsolutePath()));
			} else {
				context.error("Mono is required to run DSL compiler. Mono not detected or specified.");
				throw new ExitException();
			}
		}
	}

	@Override
	public void run(final Context context) {
	}

	@Override
	public String getShortDescription() {
		return "Path or localhost port to DSL Platform compiler. .NET or Mono is required to run the compiler";
	}

	@Override
	public String getDetailedDescription() {
		return "DSL Platform compiler.\n" +
				"Requires .NET/Mono to run.\n" +
				"\n" +
				"Example:\n" +
				"\tcompiler\n" +
				"\tcompiler=/var/dsl-platform/dsl-compiler.exe\n" +
				"\tcompiler=12345\n";
	}
}
