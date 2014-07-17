package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.*;

import java.io.File;

public enum ScalaPath implements CompileParameter {
	INSTANCE;

	public static Either<String> findCompiler(final Context context) {
		if (context.contains(InputParameter.SCALA)) {
			final File scalac = new File(context.get(InputParameter.SCALA), "scalac");
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
				return Either.success(envSH + "/bin/scalac");
			}
			return Either.fail("Unable to find Scala compiler. Add it to path or specify scala compile option.");
		}
	}

	@Override
	public boolean check(final Context context) {
		if (context.contains(InputParameter.SCALA)) {
			final String path = context.get(InputParameter.SCALA);
			final File scalac = new File(path, "scalac");
			if (!Utils.testCommand(context, scalac.getAbsolutePath(), "Usage: scalac")) {
				context.error("scala parameter is set, but Scala compiler not found/doesn't work. Please check specified scala parameter.");
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
				"	/var/user/scala-2.11\n" +
				"where /var/user/scala-2.11/scalac and /var/user/scala-2.11/jar exists";
	}
}
