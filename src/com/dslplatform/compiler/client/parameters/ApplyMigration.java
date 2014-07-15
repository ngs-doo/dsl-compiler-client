package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.*;

import java.io.Console;
import java.io.File;
import java.util.Map;

public enum ApplyMigration implements CompileParameter {
	INSTANCE;

	@Override
	public boolean check(final Map<InputParameter, String> parameters) {
		if (parameters.containsKey(InputParameter.APPLY_MIGRATION)) {
			if(!parameters.containsKey(InputParameter.CONNECTION_STRING)) {
				System.out.println("Connection string is required to apply migration script");
				System.exit(0);
			}
			if (!parameters.containsKey(InputParameter.MIGRATION)) {
				parameters.put(InputParameter.MIGRATION, null);
			}
		}
		return true;
	}

	private final static String DESCRIPTION_START = "/*MIGRATION_DESCRIPTION";
	private final static String DESCRIPTION_END = "MIGRATION_DESCRIPTION*/";

	private static boolean hasDestructive(final String[] descriptions) {
		for (int i = 1; i < descriptions.length; i+=2) {
			if (descriptions[i].startsWith("--REMOVE:")) {
				return true;
			}
		}
		return false;
	}

	private static void explainMigrations(final String[] descriptions) {
		for (int i = 2; i < descriptions.length; i+=2) {
			System.out.println(descriptions[i]);
		}
	}

	@Override
	public void run(final Map<InputParameter, String> parameters) {
		if (parameters.containsKey(InputParameter.APPLY_MIGRATION)) {
			final File file = Migration.getMigrationFile();
			final Either<String> trySql = Utils.readFile(file);
			if (!trySql.isSuccess()) {
				System.out.println("Error reading sql migration file.");
				System.out.println(trySql.whyNot());
				System.exit(0);
			}
			final String sql = trySql.get();
			if (sql.length() == 0) {
				System.out.println("Nothing to apply.");
				return;
			}
			final int start = sql.indexOf(DESCRIPTION_START);
			final int end = sql.indexOf(DESCRIPTION_END);
			if (end > start) {
				final String[] descriptions = sql.substring(start + DESCRIPTION_START.length(), end).split("\n");
				if (descriptions.length > 2) {
					explainMigrations(descriptions);
					if (hasDestructive(descriptions)) {
						System.out.println();
						System.out.println("Destructive migration detected.");
						if (parameters.containsKey(InputParameter.FORCE_MIGRATION)) {
							System.out.println("Applying destructive migration due to force option.");
						} else {
							final Console console = System.console();
							if (console == null) {
								System.out.println("Console not detected. Use force option to apply database migration.");
								System.exit(0);
							}
							System.out.print("Apply migration (y/N):");
							final String input = console.readLine();
							if (!"y".equalsIgnoreCase(input)) {
								System.out.println("Migration canceled.");
								System.exit(0);
							}
						}
					}
				}
				System.out.println("Applying migration...");
				DbConnection.execute(parameters, sql);
			} else {
				System.out.println("Migration description missing from SQL migration.");
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
		return null;
	}
}
