package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.*;

import java.util.Collections;

public enum Maven implements CompileParameter {
	INSTANCE;

	@Override
	public String getAlias() { return "maven"; }
	@Override
	public String getUsage() { return "path"; }

	public static Either<String> findMaven(final Context context) {
		if (context.contains(INSTANCE)) {
			return Either.success(context.get(INSTANCE));
		}
		final String env = System.getenv("M2");
		if (env != null && Utils.testCommand(context, env, "Apache Maven", Collections.singletonList("--version"))) {
			return Either.success(env);
		}
		if (env != null && Utils.isWindows() && !env.toLowerCase().endsWith(".bat")
				&& Utils.testCommand(context, env + ".bat", "Apache Maven", Collections.singletonList("--version"))) {
			return Either.success(env + ".bat");
		}
		if (Utils.testCommand(context, "mvn", "Apache Maven", Collections.singletonList("--version"))) {
			return Either.success("mvn");
		}
		if (Utils.isWindows() && Utils.testCommand(context, "mvn.bat", "Apache Maven", Collections.singletonList("--version"))) {
			return Either.success("mvn.bat");
		}
		return Either.fail("Unable to find mvn. Add it to path or specify maven compile option.");
	}

	@Override
	public boolean check(final Context context) {
		if (context.contains(INSTANCE)) {
			final String mvn = context.get(INSTANCE);
			if (!Utils.testCommand(context, mvn, "Apache Maven", Collections.singletonList("--version"))) {
				context.error("maven parameter is set, but Apache Maven not found/doesn't work. Please check specified maven parameter.");
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
		return "specify custom Apache Maven";
	}

	@Override
	public String getDetailedDescription() {
		return "Apache Maven can be used to download dependencies for Java libraries.\n" +
				"If mvn is not in path, this option can be used to specify Maven directory.\n" +
				"\n" +
				"If Maven is not used, dependencies can be downloaded from DSL Platform.\n" +
				"If set, M2 environment parameter will be checked." +
				"\n" +
				"Example:\n" +
				"	C:/apache-maven-2.2.1/bin/mvn.bat";
	}
}
