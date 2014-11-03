package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.*;

import java.io.File;

public enum ApplyMigration implements CompileParameter {
	INSTANCE;

	private static boolean hasDestructive(final String[] descriptions) {
		for (int i = 1; i < descriptions.length; i += 2) {
			final String desc = descriptions[i];
			if (desc.startsWith("--REMOVE:") || desc.startsWith("--UNKNOWN:")) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean check(final Context context) throws ExitException {
		if (context.contains(InputParameter.APPLY_MIGRATION)) {
			if (!context.contains(InputParameter.CONNECTION_STRING)) {
				context.error("Connection string is required to apply migration script");
				throw new ExitException();
			}
			if (!context.contains(InputParameter.MIGRATION)) {
				context.put(InputParameter.MIGRATION, null);
			}
		}
		return true;
	}

	@Override
	public void run(final Context context) throws ExitException {
		if (context.contains(InputParameter.APPLY_MIGRATION)) {
			final File file = Migration.getMigrationFile(context);
			if (file == null) {
				context.error("Can't find SQL migration file. Unable to apply database migration.");
				throw new ExitException();
			}
			final Either<String> trySql = Utils.readFile(file);
			if (!trySql.isSuccess()) {
				context.error("Error reading sql migration file.");
				context.error(trySql.whyNot());
				throw new ExitException();
			}
			final String sql = trySql.get();
			if (sql.length() == 0) {
				context.show("Nothing to apply.");
				return;
			}
			final String[] descriptions = Migration.extractDescriptions(sql);
			if (descriptions.length > 2) {
				for (int i = 2; i < descriptions.length; i += 2) {
					context.show(descriptions[i]);
				}
				if (hasDestructive(descriptions)) {
					context.show();
					context.show("Destructive migration detected.");
					if (context.contains(InputParameter.FORCE_MIGRATION)) {
						context.show("Applying destructive migration due to force option.");
					} else {
						if (!context.canInteract()) {
							context.error("Use force option to apply database migration.");
							throw new ExitException();
						}
						final String input = context.ask("Apply migration (y/N):");
						if (!"y".equalsIgnoreCase(input)) {
							context.error("Migration canceled.");
							throw new ExitException();
						}
					}
				}
			}
			context.show("Applying migration...");
			DbConnection.execute(context, sql);
			if (file.renameTo(new File(file.getParentFile(), "applied-" + file.getName()))) {
				context.show("Database migrated and script renamed to: applied-" + file.getName());
			} else {
				context.show("Database migrated, but unable to rename script: " + file.getName());
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
				"connect to the database and apply the script manually.\n" +
				"\n" +
				"Ownership of the database or superuser is required for the apply migration to work.";
	}
}
