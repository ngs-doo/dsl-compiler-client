package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.Context;

public enum LogOutput implements CompileParameter {
	INSTANCE;

	@Override
	public String getAlias() { return "log"; }
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
		return "Show detailed log";
	}

	@Override
	public String getDetailedDescription() {
		return "Show exceptions with full stacktrace.\n" +
				"Show process output.\n" +
				"Show full DSL when added or removed.";
	}
}
