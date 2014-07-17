package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.Context;
import com.dslplatform.compiler.client.InputParameter;

public enum ForceMigration implements CompileParameter {
	INSTANCE;

	@Override
	public boolean check(final Context context) {
		if (context.contains(InputParameter.FORCE_MIGRATION)) {
			if (!context.contains(InputParameter.APPLY_MIGRATION)) {
				context.error("Force migration can only be used with the apply migration option");
				return false;
			}
		}
		return true;
	}

	@Override
	public void run(final Context context) {
	}

	@Override
	public String getShortDescription() {
		return "Should destructive migrations be applied on the database without prompt?";
	}

	@Override
	public String getDetailedDescription() {
		return "If destructive database migration is detected (one which can't be reverted, such as dropping of a column, table or schema),\n" +
				"special confirmation is required for automatic application of database changes with the apply command.";
	}
}
