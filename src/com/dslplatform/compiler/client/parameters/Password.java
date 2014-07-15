package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.InputParameter;

import java.io.Console;
import java.util.Map;

public enum Password implements CompileParameter {
	INSTANCE;

	public static void retryInput(final Map<InputParameter, String> parameters) {
		System.out.print("DSL Platform password: ");
		char[] pass = System.console().readPassword();
		if (pass.length == 0) {
			return;
		}
		parameters.put(InputParameter.PASSWORD, new String(pass));
	}

	public static String getOrLoad(final Map<InputParameter, String> parameters) {
		String value = parameters.get(InputParameter.PASSWORD);
		if (value == null) {
			if(!Prompt.canUsePrompt()) {
				System.out.println("Password missing. Specify password as argument.");
				System.exit(0);
			}
			System.out.print("DSL Platform password: ");
			char[] pass = System.console().readPassword();
			value = new String(pass);
			parameters.put(InputParameter.PASSWORD, value);
		}
		return value;
	}

	@Override
	public boolean check(final Map<InputParameter, String> parameters) {
		return true;
	}

	@Override
	public void run(final Map<InputParameter, String> parameters) {
	}

	@Override
	public String getShortDescription() {
		return "password for DSL Platform account";
	}

	@Override
	public String getDetailedDescription() {
		return null;
	}
}
