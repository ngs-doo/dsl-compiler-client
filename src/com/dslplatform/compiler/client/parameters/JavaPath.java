package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.Either;
import com.dslplatform.compiler.client.InputParameter;
import com.dslplatform.compiler.client.Utils;

import java.io.File;
import java.util.Map;

public enum JavaPath implements CompileParameter {
	INSTANCE;

	public static Either<String> findCompiler(final Map<InputParameter, String> parameters) {
		if (parameters.containsKey(InputParameter.JAVA)) {
			final File javac = new File(parameters.get(InputParameter.JAVA), "javac");
			return Either.success(javac.getAbsolutePath());
		} else {
			if (Utils.testCommand("javac -help", "Usage: javac")) {
				return Either.success("javac");
			}
			return Either.fail("Unable to find Java compiler. Add it to path or specify java compile option.");
		}
	}

	public static Either<String> findArchive(final Map<InputParameter, String> parameters) {
		if (parameters.containsKey(InputParameter.JAVA)) {
			final File javac = new File(parameters.get(InputParameter.JAVA), "jar");
			return Either.success(javac.getAbsolutePath());
		} else {
			if (Utils.testCommand("jar -help", "Usage: jar")) {
				return Either.success("jar");
			}
			return Either.fail("Unable to find Java archive tool. Add it to path or specify java compile option.");
		}
	}

	@Override
	public boolean check(final Map<InputParameter, String> parameters) {
		if (parameters.containsKey(InputParameter.JAVA)) {
			final String path = parameters.get(InputParameter.JAVA);
			final File javac = new File(path, "javac");
			if (!Utils.testCommand(javac.getAbsolutePath(), "Usage: javac")) {
				System.out.println("java parameter is set, but Java compiler not found/doesn't work. Please check specified java parameter.");
				return false;
			}
			final File jar = new File(path, "jar");
			if (!Utils.testCommand(jar.getAbsolutePath(), "Usage: jar")) {
				System.out.println("java parameter is set, but Java archive tool not found/doesn't work. Please check specified java parameter.");
				return false;
			}
		}
		return true;
	}

	@Override
	public void run(final Map<InputParameter, String> parameters) {
	}

	@Override
	public String getShortDescription() {
		return "specify custom path to Java compiler (javac) and Java archive tool (jar)";
	}

	@Override
	public String getDetailedDescription() {
		return "To compile Java libraries Java compiler is required.\n" +
				"If javac is not available in path, custom path can be used to specify it.\n" +
				"jar is required to package compiled .class files into .jar" +
				"\n" +
				"Example:\n" +
				"	/var/user/java-8\n" +
				"where /var/user/java-8/javac and /var/user/java-8/jar exists";
	}
}
