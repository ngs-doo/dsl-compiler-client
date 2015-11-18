package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.Context;

public enum VarraySize implements CompileParameter {
	INSTANCE;

	@Override
	public String getAlias() {
		return "varray";
	}

	@Override
	public String getUsage() {
		return "size";
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
		return "Specify custom Oracle VARRAY size. Default is 32768";
	}

	@Override
	public String getDetailedDescription() {
		return "Oracle VARRAY types are created AS VARRAY(32768) by default.\n" +
				"Use this option to specify custom varray size";
	}
}
