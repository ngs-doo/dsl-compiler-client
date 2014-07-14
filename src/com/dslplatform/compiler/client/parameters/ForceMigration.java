package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.Either;
import com.dslplatform.compiler.client.InputParameter;
import com.dslplatform.compiler.client.Utils;

import java.io.File;
import java.util.Map;

public enum ForceMigration implements CompileParameter {
	INSTANCE;

	@Override
	public boolean check(final Map<InputParameter, String> parameters) {
		if (parameters.containsKey(InputParameter.FORCE_MIGRATION)) {
			if(!parameters.containsKey(InputParameter.APPLY_MIGRATION)) {
				System.out.println("Force migration can only be used with the apply migration option");
				System.exit(0);
			}
		}
		return true;
	}

	@Override
	public void run(final Map<InputParameter, String> parameters) {
	}

	@Override
	public String getShortDescription() {
		return "Should destructive migrations be applied on the database without prompt?";
	}

	@Override
	public String getDetailedDescription() {
		return null;
	}
}
