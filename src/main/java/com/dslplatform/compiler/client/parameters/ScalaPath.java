package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.*;

import java.io.File;

public enum ScalaPath implements CompileParameter {
	INSTANCE;

	@Override
	public String getAlias() { return "scalac"; }
	@Override
	public String getUsage() { return "file"; }

	public static Either<String> findCompiler(final Context context) {
		if (context.contains(INSTANCE)) {
			final File scalac = new File(context.get(INSTANCE));
			return Either.success(scalac.getAbsolutePath());
		} else {
			if (Utils.testCommand(context, "scalac", "Usage: scalac")) {
				return Either.success("scalac");
			}
			final String envSH = System.getenv("SCALA_HOME");
			if (envSH != null && Utils.testCommand(context, envSH + "/bin/scalac", "Usage: scalac")) {
				return Either.success(envSH + "/bin/scalac");
			}
			if(Utils.isWindows() && envSH != null && Utils.testCommand(context, envSH + "/bin/scalac.bat", "Usage: scalac")) {
				return Either.success(envSH + "/bin/scalac.bat");
			}
			if (Utils.isWindows() && Utils.testCommand(context, "scalac.bat", "Usage: scalac")) {
				return Either.success("scalac.bat");
			}
			return Either.fail("Unable to find the Scala compiler. Add it to path or specify scala compile option.");
		}
	}

	@Override
	public boolean check(final Context context) {
		if (context.contains(INSTANCE)) {
			final String path = context.get(INSTANCE);
			final File scalac = new File(path);
			if (!Utils.testCommand(context, scalac.getAbsolutePath(), "Usage: scalac")) {
				context.error("scalac parameter is set, but Scala compiler not found/doesn't work. Please check specified scalac parameter.");
				context.error("Trying to use: " + scalac.getAbsolutePath());
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
		return "specify custom path to Scala compiler (scalac)";
	}

	@Override
	public String getDetailedDescription() {
		return "To compile Scala libraries Scala compiler is required.\n" +
				"If scalac is not available in path, custom path can be used to specify it.\n" +
				"jar from Java compiler is required to package compiled .class files into .jar" +
				"\n" +
				"SCALA_HOME environment variables will be checked for Scala tools.\n" +
				"\n" +
				"Example:\n" +
				"	/var/user/scala-2.11/scalac";
	}
}
