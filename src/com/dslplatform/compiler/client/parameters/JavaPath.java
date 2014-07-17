package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.*;

import java.io.File;
import java.util.List;

public enum JavaPath implements CompileParameter {
	INSTANCE;

	public static Either<String> findCompiler(final Context context) {
		if (context.contains(InputParameter.JAVA)) {
			final File javac = new File(context.get(InputParameter.JAVA), "javac");
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
			final File source,
			final File classOut,
			final File output,
			final List<File> classPaths) {
		final String jar;
		if (context.contains(InputParameter.JAVA)) {
			final File jarFile = new File(context.get(InputParameter.JAVA), "jar");
			jar = jarFile.getAbsolutePath();
		} else {
			final String envJH = System.getenv("JAVA_HOME");
			final String envJDH = System.getenv("JDK_HOME");
			if (!Utils.testCommand(context, "jar", "Usage: jar")) {
				jar = "jar";
			} else if (envJH != null && Utils.testCommand(context, envJH + "/bin/jar", "Usage: jar")) {
				jar = envJH + "/bin/jar";
			} else if (envJDH != null && Utils.testCommand(context, envJDH + "/bin/jar", "Usage: jar")) {
				jar = envJDH + "/bin/jar";
			}
			else {
				return Either.fail("Unable to find Java archive tool. Add it to path or specify java compile option.");
			}
		}
		final int len = source.getAbsolutePath().length() + 1;
		final char separatorChar = Utils.isWindows() ? '\\' : '/';
		final StringBuilder jarCommand = new StringBuilder(jar);
		jarCommand.append(" cf \"").append(output.getAbsolutePath()).append("\"");
		for(final File f : classPaths) {
			jarCommand.append(" ").append(f.getAbsolutePath().substring(len)).append(separatorChar).append("*.class");
		}
		context.start("Running jar for " + output.getName() + " ");
		final Either<Utils.CommandResult> execArchive = Utils.runCommand(jarCommand.toString(), classOut);
		if (!execArchive.isSuccess()) {
			return Either.fail(execArchive.whyNot());
		}
		final Utils.CommandResult archiving = execArchive.get();
		if (archiving.error.length() > 0) {
			return Either.fail(archiving.error);
		}
		return Either.success(execArchive.get());
	}

	@Override
	public boolean check(final Context context) {
		if (context.contains(InputParameter.JAVA)) {
			final String path = context.get(InputParameter.JAVA);
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
