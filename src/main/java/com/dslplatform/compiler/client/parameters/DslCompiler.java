package com.dslplatform.compiler.client.parameters;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.Context;
import com.dslplatform.compiler.client.DslServer;
import com.dslplatform.compiler.client.Either;
import com.dslplatform.compiler.client.ExitException;
import com.dslplatform.compiler.client.ParameterParser;
import com.dslplatform.compiler.client.Utils;

public enum DslCompiler implements CompileParameter, ParameterParser {
	INSTANCE;

	@Override
	public String getAlias() {
		return "compiler";
	}

	@Override
	public String getUsage() {
		return "path";
	}

	private final static Charset utf8 = Charset.forName("UTF-8");

	public static Map<String, String> compile(
			final Context context,
			final File compiler,
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
			context.error(result.explainError());
			throw new ExitException();
		}
		if (result.get().exitCode != 0) {
			context.error(result.get().output);
			throw new ExitException();
		}
		final Either<Document> xml = Utils.readXml(new ByteArrayInputStream(result.get().output.getBytes(utf8)));
		if (!xml.isSuccess()) {
			context.error(result.get().output);
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
			context.log(result.get().output);
			throw new ExitException();
		}
		return files;
	}

	public static Either<String> migration(
			final Context context,
			final String version,
			final Map<String, String> previousDsls,
			final List<File> currentDsls) throws ExitException {
		final File compiler = new File(context.get(INSTANCE));
		final List<String> arguments = new ArrayList<String>();
		arguments.add("target=postgres" + version);
		if (previousDsls != null) {
			final StringBuilder oldDsl = new StringBuilder();
			for (final String v : previousDsls.values()) {
				oldDsl.append(v);
			}
			final File previousDsl = new File(TempPath.getTempProjectPath(context), "old.dsl");
			try {
				Utils.saveFile(previousDsl, oldDsl.toString());
			} catch (IOException ex) {
				context.error("Unable to save old DSL version for comparison.");
				return Either.fail(ex);
			}
			arguments.add("previous-dsl=" + previousDsl.getAbsolutePath());
		}
		for (final File f : currentDsls) {
			arguments.add("dsl=" + f.getAbsolutePath());
		}
		context.log("Creating SQL migration...");
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
			return Either.fail(result.get().output);
		}
		return Either.success(result.get().output);
	}

	public static Either<Boolean> parse(final Context context, final List<File> dsls) throws ExitException {
		final File compiler = new File(context.get(INSTANCE));
		final List<String> arguments = new ArrayList<String>();
		for (final File f : dsls) {
			arguments.add("dsl=" + f.getAbsolutePath());
		}
		context.log("Parsing DSL...");
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
			return Either.fail(result.get().output);
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
		if (!context.contains(INSTANCE)) {
			return true;
		}
		final String value = context.get(INSTANCE);
		final boolean isEmpty = value == null || value.length() == 0;
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
			try {
				DslServer.downloadAndUnpack(context, "dsl-compiler", tempPath);
			} catch (final IOException ex) {
				context.error("Error downloading compiler from https://dsl-platform.com");
				context.error(ex);
				throw new ExitException();
			}
			if (!testCompiler(context, compiler)) {
				context.error("Specified compiler is invalid: " + path.getAbsolutePath());
				throw new ExitException();
			}
			context.put(INSTANCE, compiler.getAbsolutePath());
		} else {
			if (!testCompiler(context, path)) {
				context.error("Specified compiler is invalid: " + path.getAbsolutePath());
				throw new ExitException();
			}
		}
		return true;
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
		return "Path to DSL Platform compiler. For offline compilation, local compiler can be specified";
	}

	@Override
	public String getDetailedDescription() {
		return "DSL Platform compiler can be downloaded for offline use.\n" +
				"It requires .NET/Mono to run.\n" +
				"\n" +
				"Example:\n" +
				"	-compiler\n" +
				"	-compiler=/var/dsl-platform/dsl-compiler.exe\n";
	}
}
