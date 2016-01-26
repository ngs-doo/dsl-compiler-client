package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.*;

import java.util.Collections;

public enum Mono implements CompileParameter {
	INSTANCE;

	@Override
	public String getAlias() { return "mono"; }
	@Override
	public String getUsage() { return "path"; }

	public static Either<String> findMono(final Context context) {
		if (context.contains(INSTANCE)) {
			return Either.success(context.get(INSTANCE));
		} else {
			if (Utils.testCommand(context, "mono", "Mono", Collections.singletonList("--version"))) {
				return Either.success("mono");
			}
			return Either.fail("Unable to find Mono. Add it to path or specify mono compile option.");
		}
	}

	@Override
	public boolean check(final Context context) {
		if (context.contains(INSTANCE)) {
			final String compiler = context.get(INSTANCE);
			if (Utils.testCommand(context, compiler, "Mono", Collections.singletonList("--version"))) {
				context.error("mono parameter is set, but Mono not found/doesn't work. Please check specified mono parameter.");
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
		return "Specify path to Mono";
	}

	@Override
	public String getDetailedDescription() {
		return "To run Mono applications, Mono is required.\n" +
				"On Windows Mono is not required, but can be used, while on Linux Mono is usually available via command line.\n" +
				"If custom path is required this option can be used to specify it.\n" +
				"\n" +
				"Example:\n" +
				"\t/var/user/mono-3.10/mono";
	}
}
