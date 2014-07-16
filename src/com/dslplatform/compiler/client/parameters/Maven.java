package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.Either;
import com.dslplatform.compiler.client.InputParameter;
import com.dslplatform.compiler.client.Utils;

import java.util.Map;

public enum Maven implements CompileParameter {
	INSTANCE;

	public static Either<String> findMaven(final Map<InputParameter, String> parameters) {
		if (parameters.containsKey(InputParameter.MAVEN)) {
			return Either.success(parameters.get(InputParameter.MAVEN));
		}
		final String env = System.getenv("M2");
		if (env != null && Utils.testCommand(env + " --version", "Apache Maven")) {
			return Either.success(env);
		}
		if (env != null && Utils.isWindows() && !env.toLowerCase().endsWith(".bat")
				&& Utils.testCommand(env + ".bat" + " --version", "Apache Maven")) {
			return Either.success(env + ".bat");
		}
		if (Utils.testCommand("mvn --version", "Apache Maven")) {
			return Either.success("mvn");
		}
		return Either.fail("Unable to find mvn. Add it to path or specify maven compile option.");
	}

	@Override
	public boolean check(final Map<InputParameter, String> parameters) {
		if (parameters.containsKey(InputParameter.MAVEN)) {
			final String mvn = parameters.get(InputParameter.MAVEN);
			if (!Utils.testCommand(mvn + " --version", "Apache Maven")) {
				System.out.println("maven parameter is set, but Apache Maven not found/doesn't work. Please check specified maven parameter.");
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
		return "specify custom Apache Maven";
	}

	@Override
	public String getDetailedDescription() {
		return "Apache Maven can be used to download dependencies for Java libraries.\n" +
				"If mvn is not in path, this option can be used to specify Maven directory.\n" +
				"\n" +
				"If Maven is not used, dependencies can be downloaded from DSL Platform.\n" +
				"\n" +
				"Example:\n" +
				"	C:/apache-maven-2.2.1/bin/mvn.bat";
	}
}
