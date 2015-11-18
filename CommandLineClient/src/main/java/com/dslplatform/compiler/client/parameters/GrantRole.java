package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.Context;

public enum GrantRole implements CompileParameter {
	INSTANCE;

	@Override
	public String getAlias() {
		return "grant";
	}

	@Override
	public String getUsage() {
		return "role";
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
		return "Specify custom Oracle GRANT TO role. Default is PUBLIC";
	}

	@Override
	public String getDetailedDescription() {
		return "To improve security for Oracle scripts, custom role can be specified.";
	}
}
