package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.*;

import java.util.Map;

public enum Prompt implements CompileParameter {
	INSTANCE;

	private static boolean usePrompt;

	public static boolean canUsePrompt() {
		return usePrompt;
	}

	@Override
	public boolean check(final Map<InputParameter, String> parameters) {
		usePrompt = !parameters.containsKey(InputParameter.NO_PROMPT) && System.console() != null;
		return true;
	}

	@Override
	public void run(final Map<InputParameter, String> parameters) {
	}

	@Override
	public String getShortDescription() {
		return "Should try to interact on missing arguments or errors.";
	}

	@Override
	public String getDetailedDescription() {
		return "For fully automated scenarios, prompt should not be invoked.\n" +
				"To cover such use case no prompt option can be used.";
	}
}
