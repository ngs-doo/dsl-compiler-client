package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.Context;

public enum ForceMigration implements CompileParameter {
	INSTANCE;

	@Override
	public String getAlias() { return "force"; }
	@Override
	public String getUsage() { return null; }

	@Override
	public boolean check(final Context context) {
		if (context.contains(INSTANCE)) {
			if (!context.contains(ApplyMigration.INSTANCE)) {
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
				"special confirmation is required for automatic application of database changes with the apply command.\n" +
				"\n" +
				"This option can also be used to create a new database if one doesn't exist.";
	}
}
