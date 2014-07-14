package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.Either;
import com.dslplatform.compiler.client.InputParameter;

import java.io.Console;
import java.util.Map;

public enum Username implements CompileParameter {
	INSTANCE;

	public static void retryInput(final Map<InputParameter, String> parameters) {
		String value = parameters.get(InputParameter.USERNAME);
		System.out.print("Username (" + value + "): ");
		value = System.console().readLine();
		if (value.length() == 0) {
			return;
		}
		if (value.contains(":")) {
			System.out.println("Invalid char (:) found in username");
			System.exit(0);
		}
		parameters.put(InputParameter.USERNAME, value);
	}

	public static Either<String> getOrLoad(final Map<InputParameter, String> parameters) {
		String value = parameters.get(InputParameter.USERNAME);
		if (value == null || value.length() == 0) {
			final Console console = System.console();
			if (console == null) {
				return Either.fail("Console not available. Specify username as argument.");
			}
			System.out.print("Username: ");
			value = console.readLine();
			parameters.put(InputParameter.USERNAME, value);
		}
		if (value.length() == 0) {
			return Either.fail("Username not provided");
		}
		if (value.contains(":")) {
			return Either.fail("Invalid char (:) found in username");
		}
		return Either.success(value);
	}

	@Override
	public boolean check(final Map<InputParameter, String> parameters) {
		if (!parameters.containsKey(InputParameter.USERNAME)) {
			return true;
		}
		Either<String> value = getOrLoad(parameters);
		if (value.isSuccess()) {
			return true;
		}
		System.out.println(value.whyNot());
		return false;
	}

	@Override
	public void run(final Map<InputParameter, String> parameters) {
	}

	@Override
	public String getShortDescription() {
		return "username for DSL Platform account";
	}

	@Override
	public String getDetailedDescription() {
		return null;
	}
}
