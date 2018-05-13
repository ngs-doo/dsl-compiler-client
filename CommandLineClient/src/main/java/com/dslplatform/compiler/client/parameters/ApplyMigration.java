package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.*;

import java.io.File;

public enum ApplyMigration implements CompileParameter {
	INSTANCE;

	@Override
	public String getAlias() {
		return "apply";
	}

	@Override
	public String getUsage() {
		return null;
	}

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
		if (context.contains(INSTANCE)) {
			if (!context.contains(PostgresConnection.INSTANCE)
					&& !context.contains(OracleConnection.INSTANCE)) {
				context.error("Connection string is required to apply migration script.\n" +
						"Neither Oracle od Postgres connection string was defined.");
				throw new ExitException();
			}
			if (!context.contains(Migration.INSTANCE)) {
				context.put(Migration.INSTANCE, null);
			}
		}
		return true;
	}

	@Override
	public void run(final Context context) throws ExitException {
		if (context.contains(INSTANCE)) {
			final File postgres = Migration.getPostgresMigrationFile(context);
			final File oracle = Migration.getOracleMigrationFile(context);
			if (postgres == null && oracle == null) {
				context.error("Can't find SQL migration file. Unable to apply database migration.");
				throw new ExitException();
			}
			if (postgres != null) {
				applyMigrationScript(context, postgres, PostgresDB);
			}
			if (oracle != null) {
				applyMigrationScript(context, oracle, OracleDB);
			}
		}
	}

	private static final DB PostgresDB = new DB() {
		@Override
		public String getDName() {
			return "Postgres";
		}

		@Override
		public void execute(Context context, String sql) throws ExitException {
			PostgresConnection.execute(context, sql);
		}
	};

	private static final DB OracleDB = new DB() {
		@Override
		public String getDName() {
			return "Oracle";
		}

		@Override
		public void execute(Context context, String sql) throws ExitException {
			OracleConnection.execute(context, sql);
		}
	};


	private interface DB {
		String getDName();

		void execute(final Context context, final String sql) throws ExitException;
	}

	private static void applyMigrationScript(final Context context, final File file, final DB db) throws ExitException {
		if ("empty.sql".equals(file.getName())) {
			context.show("Nothing to apply.");
			return;
		}
		final Either<String> trySql = Utils.readFile(file);
		if (!trySql.isSuccess()) {
			context.error("Error reading SQL migration file for " + db.getDName() + ".");
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
				context.show("Destructive migration detected for " + db.getDName() + ".");
				if (context.contains(Force.INSTANCE)) {
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
		db.execute(context, sql);
		final String customFile = context.get("sql:" + db.getDName().toLowerCase());
		if (customFile != null && file.getName().equals(customFile)) {
			context.show("Database migrated via: " + file.getAbsolutePath());
		} else if (file.renameTo(new File(file.getParentFile(), "applied-" + file.getName()))) {
			context.show("Database migrated and script renamed to: applied-" + file.getName());
		} else {
			context.show("Database migrated, but unable to rename script: " + file.getName());
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
