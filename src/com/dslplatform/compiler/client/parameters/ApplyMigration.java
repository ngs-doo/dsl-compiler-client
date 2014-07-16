package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.*;

import java.io.File;

public enum ApplyMigration implements CompileParameter {
	INSTANCE;

	@Override
	public boolean check(final Context context) {
		if (context.contains(InputParameter.APPLY_MIGRATION)) {
			if (!context.contains(InputParameter.CONNECTION_STRING)) {
				context.error("Connection string is required to apply migration script");
				System.exit(0);
			}
			if (!context.contains(InputParameter.MIGRATION)) {
				context.put(InputParameter.MIGRATION, null);
			}
		}
		return true;
	}

	private final static String DESCRIPTION_START = "/*MIGRATION_DESCRIPTION";
	private final static String DESCRIPTION_END = "MIGRATION_DESCRIPTION*/";

	private static boolean hasDestructive(final String[] descriptions) {
		for (int i = 1; i < descriptions.length; i += 2) {
			final String desc = descriptions[i];
			if (desc.startsWith("--REMOVE:") || desc.startsWith("--UNKNOWN:")) {
				return true;
			}
		}
		return false;
	}

	private static void explainMigrations(final String[] descriptions, final Context context) {
		for (int i = 2; i < descriptions.length; i += 2) {
			context.log(descriptions[i]);
		}
	}

	@Override
	public void run(final Context context) {
		if (context.contains(InputParameter.APPLY_MIGRATION)) {
			final File file = Migration.getMigrationFile(context);
			final Either<String> trySql = Utils.readFile(file);
			if (!trySql.isSuccess()) {
				context.error("Error reading sql migration file.");
				context.error(trySql.whyNot());
				System.exit(0);
			}
			final String sql = trySql.get();
			if (sql.length() == 0) {
				context.log("Nothing to apply.");
				return;
			}
			final int start = sql.indexOf(DESCRIPTION_START);
			final int end = sql.indexOf(DESCRIPTION_END);
			if (end > start) {
				final String[] descriptions = sql.substring(start + DESCRIPTION_START.length(), end).split("\n");
				if (descriptions.length > 2) {
					explainMigrations(descriptions, context);
					if (hasDestructive(descriptions)) {
						context.log();
						context.log("Destructive migration detected.");
						if (context.contains(InputParameter.FORCE_MIGRATION)) {
							context.log("Applying destructive migration due to force option.");
						} else {
							if (!context.canInteract()) {
								context.error("Use force option to apply database migration.");
								System.exit(0);
							}
							final String input = context.ask("Apply migration (y/N):");
							if (!"y".equalsIgnoreCase(input)) {
								context.error("Migration canceled.");
								System.exit(0);
							}
						}
					}
				}
				context.log("Applying migration...");
				DbConnection.execute(context, sql);
			} else {
				context.error("Migration description missing from SQL migration.");
				System.exit(0);
			}
		}
	}

	@Override
	public String getShortDescription() {
		return "Apply migration on the database after creating the migration script";
	}

	@Override
	public String getDetailedDescription() {
		return "DSL Platform will compare previously applied DSL with the current one and provide a migration SQL script.\n" +
				"When apply option is enabled, SQL script will be applied to the database directly.\n" +
				"This helps with the workflow during early development, so that developer doesn't need to inspect the script,\n" +
				"connect to the database and apply it on it.\n" +
				"\n" +
				"Ownership of the database or superuser is required for the apply migration to work.";
	}
}
