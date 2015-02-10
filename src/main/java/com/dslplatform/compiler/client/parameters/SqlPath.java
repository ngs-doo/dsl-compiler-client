package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.Context;
import com.dslplatform.compiler.client.ExitException;

import java.io.File;

public enum SqlPath implements CompileParameter {
	INSTANCE;

	@Override
	public String getAlias() { return "sql"; }
	@Override
	public String getUsage() { return "path"; }

	@Override
	public boolean check(final Context context) throws ExitException {
		if (context.contains(INSTANCE)) {
			final String value = context.get(INSTANCE);
			if (value != null && value.length() > 0) {
				final File sqlPath = new File(value);
				if (!sqlPath.exists()) {
					context.error("SQL path provided (" + sqlPath.getAbsolutePath() + ") but doesn't exists.");
					if (!context.canInteract()) {
						context.error("Specify existing path or remove parameter to use temporary folder.");
						return false;
					} else {
						final String answer = context.ask("Create directory for SQL scripts (" + sqlPath.getAbsolutePath() + ") (y/N):");
						if (!"y".equalsIgnoreCase(answer)) {
							throw new ExitException();
						}
						if (!sqlPath.mkdirs()) {
							context.error("Failed to create SQL folder.");
							throw new ExitException();
						}
					}
				} else if (!sqlPath.isDirectory()) {
					context.error("SQL path provided (" + sqlPath.getAbsolutePath() + ") but it's not a directory.");
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public void run(final Context context) {
	}

	@Override
	public String getShortDescription() {
		return "Where to save SQL migration";
	}

	@Override
	public String getDetailedDescription() {
		return "SQL migration script which contains DDL changes..\n" +
				"When deploying changes to the production, previously created SQL script should be applied.\n" +
				"SQL path can be specified so created/applied SQL scripts can be stored and used later.";
	}
}
