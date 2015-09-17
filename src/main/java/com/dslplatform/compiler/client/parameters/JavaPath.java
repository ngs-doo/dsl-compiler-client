package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.*;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public enum JavaPath implements CompileParameter {
	INSTANCE;

	@Override
	public String getAlias() {
		return "java";
	}

	@Override
	public String getUsage() {
		return "path";
	}

	private static final String CACHE_FILE_PREFIX = "java_path_cache_";

	public static Either<String> findCompiler(final Context context) {
		return getCommand(context, "javac", "Java compiler");
	}

	private static Either<String> getJarCommand(final Context context) {
		return getCommand(context, "jar", "Java archive tool");
	}

	private static Either<String> getCommand(final Context context, final String name, final String description) {
		if (context.contains(INSTANCE)) {
			final String file = context.load(CACHE_FILE_PREFIX + name);
			return Either.success(file);
		}
		final String envJH = System.getenv("JAVA_HOME");
		final String envJDK = System.getenv("JDK_HOME");
		final Either<String> path = Utils.findCommand(context, null, name, "Usage: " + name);
		if (path.isSuccess()) {
			context.cache(CACHE_FILE_PREFIX + name, path.get());
			return Either.success(path.get());
		}
		if (envJH != null) {
			final Either<String> homePath = Utils.findCommand(context, new File(envJH, "bin").getPath(), name, "Usage: " + name);
			context.cache(CACHE_FILE_PREFIX + name, homePath.get());
			return Either.success(homePath.get());
		}
		if (envJDK != null) {
			final Either<String> homePath = Utils.findCommand(context, new File(envJDK, "bin").getPath(), name, "Usage: " + name);
			context.cache(CACHE_FILE_PREFIX + name, homePath.get());
			return Either.success(homePath.get());
		}
		return Either.fail("Unable to find " + description + ". Add it to path or specify java compile option.");
	}

	private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd-HH-mm");

	public static synchronized Either<Utils.CommandResult> makeArchive(
			final Context context,
			final File classOut,
			final File output,
			final Map<String, List<String>> services) {
		final Either<String> tryJar = getJarCommand(context);
		if (!tryJar.isSuccess()) {
			return Either.fail(tryJar.whyNot());
		}
		final String jar = tryJar.get();

		final List<String> jarArguments = makeJarArguments(context, classOut, "class", output, services);

		final File metaInf = new File(classOut, "META-INF");
		final File manifest = new File(metaInf, "MANIFEST.MF");
		if (!metaInf.exists()) {
			if (!metaInf.mkdirs()) {
				return Either.fail("Error creating: " + metaInf.getAbsolutePath());
			}
		}
		try {
			final String version = context.contains(Version.INSTANCE)
					? context.get(Version.INSTANCE)
					: DATE_FORMAT.format(new Date());
			Utils.saveFile(context, manifest, "Implementation-Version: " + version + "\n");
		} catch (IOException e) {
			context.error("Can't create manifest: " + manifest);
			return Either.fail(e);
		}
		if (services != null) {
			final File servicePath = new File(metaInf, "services");
			if (!servicePath.exists()) {
				if (!servicePath.mkdirs()) {
					return Either.fail("Error creating: " + servicePath.getAbsolutePath());
				}
			}
			for (Map.Entry<String, List<String>> kv : services.entrySet()) {
				final File service = new File(servicePath, kv.getKey());
				try {
					StringBuilder sb = new StringBuilder();
					for (String it : kv.getValue()) {
						sb.append(it);
						sb.append("\n");
					}
					Utils.saveFile(context, service, sb.toString());
				} catch (IOException e) {
					context.error("Can't create service: " + kv);
					return Either.fail(e);
				}
			}
		}
		context.show("Running jar for " + output.getName() + "...");
		final Either<Utils.CommandResult> execArchive = Utils.runCommand(context, jar, classOut, jarArguments);
		if (!execArchive.isSuccess()) {
			return Either.fail(execArchive.whyNot());
		}
		final Utils.CommandResult archiving = execArchive.get();
		if (archiving.error.length() > 0) {
			return Either.fail(archiving.error);
		}

		return Either.success(execArchive.get());
	}

	public static Either<Utils.CommandResult> makeEmptyArchive(Context context, final File classOut, File output) {
		final Either<String> tryJar = getJarCommand(context);
		if (!tryJar.isSuccess())
			return Either.fail(tryJar.whyNot());
		final String jar = tryJar.get();

		final String manifestName = "MANIFEST.MF";
		try {
			final File mockManifest = new File(new File(classOut, "META-INF"), "MANIFEST.MF");
			Utils.saveFile(context, mockManifest, "Manifest-Version: 1.0");
		} catch (IOException e) {
			context.error("Can't create mock MANIFEST.MF.");
			return Either.fail(e);
		}

		final List<String> jarArguments = new ArrayList<String>();
		jarArguments.add("cfm");
		jarArguments.add(output.getAbsolutePath());
		jarArguments.add(manifestName);
		return Utils.runCommand(context, jar, classOut, jarArguments);
	}

	private static List<String> makeJarArguments(
			final Context context,
			final File source,
			final String type,
			final File output,
			final Map<String, List<String>> services) {
		final List<String> jarArguments = new ArrayList<String>();
		jarArguments.add("cfm");
		jarArguments.add(output.getAbsolutePath());
		jarArguments.add("META-INF" + File.separator + "MANIFEST.MF");

		if (services != null && !services.isEmpty()) {
			if (Utils.isWindows()) {
				jarArguments.add("META-INF" + File.separator + "services" + File.separator + "*");
			} else {
				for (String key : services.keySet()) {
					jarArguments.add("META-INF" + File.separator + "services" + File.separator + key);
				}
			}
		}

		final int len = source.getAbsolutePath().length() + 1;
		if (Utils.isWindows()) {
			final List<File> classDirs = Utils.findNonEmptyDirs(source, "." + type);
			for (final File f : classDirs) {
				jarArguments.add(f.getAbsolutePath().substring(len) + File.separator + "*." + type);
			}
		} else {
			final List<File> classFiles = Utils.findFiles(context, source, Collections.singletonList("." + type));
			for (final File f : classFiles) {
				jarArguments.add(f.getAbsolutePath().substring(len));
			}
		}
		return jarArguments;
	}

	@Override
	public boolean check(final Context context) {
		if (context.contains(INSTANCE)) {
			final String path = context.get(INSTANCE);
			final Either<String> javac = Utils.findCommand(context, path, "javac", "Usage: javac");
			if (!javac.isSuccess()) {
				context.error("java parameter is set, but Java compiler not found/doesn't work. Please check specified java parameter.");
				context.error("Trying to find javac in " + path);
				return false;
			}
			final Either<String> jar = Utils.findCommand(context, path, "jar", "Usage: jar");
			if (!jar.isSuccess()) {
				context.error("java parameter is set, but Java archive tool not found/doesn't work. Please check specified java parameter.");
				context.error("Trying to find jar in " + path);
				return false;
			}
			context.cache(CACHE_FILE_PREFIX + "javac", javac.get());
			context.cache(CACHE_FILE_PREFIX + "jar", jar.get());
		}
		return true;
	}

	@Override
	public void run(final Context context) {
	}

	@Override
	public String getShortDescription() {
		return "specify custom path to Java compiler (javac) and Java archive tool (jar)";
	}

	@Override
	public String getDetailedDescription() {
		return "To compile Java libraries a Java compiler is required.\n" +
				"If javac is not available in the path, custom path can be used to specify it.\n" +
				"jar is required to package compiled .class files into .jar\n" +
				"\n" +
				"JDK_HOME and JAVA_HOME environment variables will be checked for Java tools.\n" +
				"\n" +
				"Example:\n" +
				"\t/var/user/java-8\n" +
				"where /var/user/java-8/javac and /var/user/java-8/jar exist";
	}
}
