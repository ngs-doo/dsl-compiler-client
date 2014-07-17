package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.Context;
import com.dslplatform.compiler.client.ExitException;
import com.dslplatform.compiler.client.InputParameter;

public enum Password implements CompileParameter {
	INSTANCE;

	public static void retryInput(final Context context) {
		char[] pass = context.askSecret("DSL Platform password:");
		if (pass.length == 0) {
			return;
		}
		context.put(InputParameter.PASSWORD, new String(pass));
	}

	public static String getOrLoad(final Context context) throws ExitException {
		String value = context.get(InputParameter.PASSWORD);
		if (value == null) {
			if(!context.canInteract()) {
				context.error("Password missing. Specify password as argument.");
				throw new ExitException();
			}
			char[] pass = context.askSecret("DSL Platform password:");
			value = new String(pass);
			context.put(InputParameter.PASSWORD, value);
		}
		return value;
	}

	@Override
	public boolean check(final Context context) {
		return true;
	}

	@Override
	public void run(final Context context) {
	}

	@Override
	public String getShortDescription() {
		return "password for DSL Platform account";
	}

	@Override
	public String getDetailedDescription() {
		return "DSL Platform account is required to compile DSL.\n" +
				"Online DSL compiler is free to use for registered users.\n" +
				"Specify password for DSL Platform account";
	}
}
