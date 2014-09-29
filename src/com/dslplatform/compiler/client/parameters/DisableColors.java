package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.Context;

public enum DisableColors implements CompileParameter {
	INSTANCE;

	@Override
	public boolean check(final Context context) {
		return true;
	}

	@Override
	public void run(final Context context) {
	}

	@Override
	public String getShortDescription() {
		return "Disable ANSI colors.";
	}

	@Override
	public String getDetailedDescription() {
		return "By default, for easier UI interaction, errors are displayed as red, logs in yellow and questions as bold white.\n" +
				"To disable ANSI coloring enable this option.";
	}
}
