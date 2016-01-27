package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.*;

public enum Prompt implements CompileParameter {
	INSTANCE;

	@Override
	public String getAlias() { return "no-prompt"; }
	@Override
	public String getUsage() { return null; }

	@Override
	public boolean check(final Context context) {
		return true;
	}

	@Override
	public void run(final Context context) {
	}

	@Override
	public String getShortDescription() {
		return "Should not interact on missing arguments or errors?";
	}

	@Override
	public String getDetailedDescription() {
		return "For fully automated scenarios, prompt should not be invoked.\n" +
				"To cover such use case no prompt option can be used.";
	}
}
