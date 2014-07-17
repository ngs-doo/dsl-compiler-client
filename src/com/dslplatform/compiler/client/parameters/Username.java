package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.*;

public enum Username implements CompileParameter {
	INSTANCE;

	public static void retryInput(final Context context) throws ExitException {
		String value = context.get(InputParameter.USERNAME);
		final String question;
		if (value != null && value.length() > 0) {
			question = "DSL Platform username (" + value + "):";
		} else {
			question = "DSL Platform username:";
		}
		value = context.ask(question);
		if (value.length() == 0) {
			if (context.get(InputParameter.USERNAME) == null) {
				context.error("Username not provided");
				throw new ExitException();
			}
			return;
		}
		if (value.contains(":")) {
			context.error("Invalid char (:) found in username");
			throw new ExitException();
		}
		context.put(InputParameter.USERNAME, value);
	}

	public static Either<String> getOrLoad(final Context context) {
		String value = context.get(InputParameter.USERNAME);
		if (value == null || value.length() == 0) {
			if (!context.canInteract()) {
				return Either.fail("Username missing. Specify username as argument.");
			}
			value = context.ask("DSL Platform username:");
			context.put(InputParameter.USERNAME, value);
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
	public boolean check(final Context context) {
		if (!context.contains(InputParameter.USERNAME)) {
			return true;
		}
		final Either<String> value = getOrLoad(context);
		if (value.isSuccess()) {
			return true;
		}
		context.error(value.whyNot());
		return false;
	}

	@Override
	public void run(final Context context) {
	}

	@Override
	public String getShortDescription() {
		return "username for DSL Platform account";
	}

	@Override
	public String getDetailedDescription() {
		return "DSL Platform account is required to compile DSL.\n" +
				"Online DSL compiler is free to use for registered users.\n" +
				"Specify username for DSL Platform account";
	}
}
