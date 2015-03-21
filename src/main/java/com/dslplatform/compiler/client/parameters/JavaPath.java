package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum JavaPath implements CompileParameter {
	INSTANCE;

	@Override
	public String getAlias() { return "java"; }
	@Override
	public String getUsage() { return "path"; }

	public static Either<String> findCompiler(final Context context) {
		if (context.contains(INSTANCE)) {
			final File javac = new File(context.get(INSTANCE), "javac");
			return Either.success(javac.getAbsolutePath());
		} else {
			final String envJH = System.getenv("JAVA_HOME");
			final String envJDH = System.getenv("JDK_HOME");
			if (Utils.testCommand(context, "javac", "Usage: javac")) {
				return Either.success("javac");
			} else if (envJH != null && Utils.testCommand(context, envJH + "/bin/javac", "Usage: javac")) {
				return Either.success(envJH + "/bin/javac");
			} else if (envJDH != null && Utils.testCommand(context, envJDH + "/bin/javac", "Usage: javac")) {
				return Either.success(envJDH + "/bin/javac");
			}
			return Either.fail("Unable to find Java compiler. Add it to path or specify java compile option.");
		}
	}

	public static Either<Utils.CommandResult> makeArchive(
			final Context context,
			final File classOut,
			final File output) {
		final Either<String> tryJar = getJarCommand(context);
		if (!tryJar.isSuccess())
			return Either.fail(tryJar.whyNot());
		final String jar = tryJar.get();

		final List<String> jarArguments = makeJarArguments(classOut, "class", output);

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
			final File mockManifest = new File(classOut, manifestName);
			Utils.saveFile(mockManifest, "Manifest-Version: 1.0");
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

	private static Either<String> getJarCommand(final Context context) {
		if (context.contains(INSTANCE)) {
			final File jarFile = new File(context.get(INSTANCE), "jar");
			return Either.success(jarFile.getAbsolutePath());
		} else {
			final String envJH = System.getenv("JAVA_HOME");
			final String envJDH = System.getenv("JDK_HOME");
			if (Utils.testCommand(context, "jar", "Usage: jar")) {
				return Either.success("jar");
			} else if (envJH != null && Utils.testCommand(context, envJH + "/bin/jar", "Usage: jar")) {
				return  Either.success(envJH + "/bin/jar");
			} else if (envJDH != null && Utils.testCommand(context, envJDH + "/bin/jar", "Usage: jar")) {
				return Either.success(envJDH + "/bin/jar");
			} else {
				return Either.fail("Unable to find Java archive tool. Add it to path or specify java compile option.");
			}
		}
	}
	private static List<String> makeJarArguments(
			final File source,
			final String type,
			final File output) {
		final List<String> jarArguments = new ArrayList<String>();
		jarArguments.add("cf");
		jarArguments.add(output.getAbsolutePath());

		final int len = source.getAbsolutePath().length() + 1;
		if (Utils.isWindows()) {
			final List<File> classDirs = Utils.findNonEmptyDirs(source, "." + type);
			for (final File f : classDirs) {
				jarArguments.add(f.getAbsolutePath().substring(len) + File.separator + "*." + type);
			}
		} else {
			final List<File> classFiles = Utils.findFiles(source, Arrays.asList("." + type));
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
			final File javac = new File(path, "javac");
			if (!Utils.testCommand(context, javac.getAbsolutePath(), "Usage: javac")) {
				context.error("java parameter is set, but Java compiler not found/doesn't work. Please check specified java parameter.");
				context.error("Trying to use: " + javac.getAbsolutePath());
				return false;
			}
			final File jar = new File(path, "jar");
			if (!Utils.testCommand(context, jar.getAbsolutePath(), "Usage: jar")) {
				context.error("java parameter is set, but Java archive tool not found/doesn't work. Please check specified java parameter.");
				context.error("Trying to use: " + jar.getAbsolutePath());
				return false;
			}
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
		return "To compile Java libraries Java compiler is required.\n" +
				"If javac is not available in path, custom path can be used to specify it.\n" +
				"jar is required to package compiled .class files into .jar\n" +
				"\n" +
				"JDK_HOME and JAVA_HOME environment variables will be checked for Java tools.\n" +
				"\n" +
				"Example:\n" +
				"	/var/user/java-8\n" +
				"where /var/user/java-8/javac and /var/user/java-8/jar exists";
	}
}
