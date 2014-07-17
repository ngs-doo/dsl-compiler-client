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
			if (Utils.testCommand("scalac -help", "Usage: scalac")) {
				return Either.success("scalac");
			}
			return Either.fail("Unable to find Scala compiler. Add it to path or specify scala compile option.");
		}
	}

	public static Either<String> findArchive(final Context context) {
		if (context.contains(InputParameter.SCALA)) {
			final File scalac = new File(context.get(InputParameter.SCALA), "jar");
			return Either.success(scalac.getAbsolutePath());
		} else {
			if (Utils.testCommand("jar -help", "Usage: jar")) {
				return Either.success("jar");
			}
			return Either.fail("Unable to find Scala archive tool. Add it to path or specify scala compile option.");
		}
	}

	@Override
	public boolean check(final Context context) {
		if (context.contains(InputParameter.SCALA)) {
			final String path = context.get(InputParameter.SCALA);
			final File scalac = new File(path, "scalac");
			if (!Utils.testCommand(scalac.getAbsolutePath(), "Usage: scalac")) {
				context.error("scala parameter is set, but Scala compiler not found/doesn't work. Please check specified scala parameter.");
				return false;
			}
			final File jar = new File(path, "jar");
			if (!Utils.testCommand(jar.getAbsolutePath(), "Usage: jar")) {
				context.error("scala parameter is set, but Scala archive tool not found/doesn't work. Please check specified scala parameter.");
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
		return "specify custom path to Scala compiler (scalac) and Scala archive tool (jar)";
	}

	@Override
	public String getDetailedDescription() {
		return "To compile Scala libraries Scala compiler is required.\n" +
				"If scalac is not available in path, custom path can be used to specify it.\n" +
				"jar is required to package compiled .class files into .jar" +
				"\n" +
				"Example:\n" +
				"	/var/user/scala-2.11\n" +
				"where /var/user/scala-2.11/scalac and /var/user/scala-2.11/jar exists";
	}
}
