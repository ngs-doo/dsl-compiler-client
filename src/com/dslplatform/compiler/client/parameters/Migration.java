package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.*;
import com.dslplatform.compiler.client.json.JsonObject;
import com.dslplatform.compiler.client.json.JsonValue;

import java.io.File;
import java.io.IOException;
import java.util.*;

public enum Migration implements CompileParameter {
	INSTANCE;

	private final static String DESCRIPTION_START = "/*MIGRATION_DESCRIPTION";
	private final static String DESCRIPTION_END = "MIGRATION_DESCRIPTION*/";

	private static final String MIGRATION_FILE_NAME = "migration_file";

	public static File getMigrationFile(final Context context) {
		return context.load(MIGRATION_FILE_NAME);
	}

	public static String[] extractDescriptions(final String sql) throws ExitException {
		final int start = sql.indexOf(DESCRIPTION_START);
		final int end = sql.indexOf(DESCRIPTION_END);
		if (end > start) {
			return sql.substring(start + DESCRIPTION_START.length(), end).split("\n");
		}
		return new String[0];
	}

	@Override
	public boolean check(final Context context) {
		if (context.contains(InputParameter.MIGRATION)) {
			if (!context.contains(InputParameter.CONNECTION_STRING)) {
				context.error("Connection string is required to create a migration script");
				return false;
			}
			if (context.contains(InputParameter.SQL)) {
				final String value = context.get(InputParameter.SQL);
				if (value == null || value.length() == 0) {
					return true;
				}
				final File sqlPath = new File(value);
				if (!sqlPath.exists()) {
					context.error("Path for SQL migration script provided (" + value + ") but not found");
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public void run(final Context context) throws ExitException {
		if (context.contains(InputParameter.MIGRATION)) {
			final Map<String, String> currentDsl = DslPath.getCurrentDsl(context);
			final Map.Entry<Map<String, String>, String> previousDslAndVersion = DbConnection.getDatabaseDslAndVersion(context);
			final String url = "Platform.svc/unmanaged/postgres-migration?version=" + previousDslAndVersion.getValue();
			final JsonObject arg =
					new JsonObject()
							.add("Old", Utils.toJson(previousDslAndVersion.getKey()))
							.add("New", Utils.toJson(currentDsl));
			context.show("Downloading SQL migration...");
			final Either<String> response = DslServer.put(url, context, arg);
			if (!response.isSuccess()) {
				context.error("Error creating SQL migration:");
				context.error(response.whyNot());
				throw new ExitException();
			}
			final String value = context.get(InputParameter.SQL);
			final File path;
			if (!context.contains(InputParameter.SQL) || value == null || value.length() == 0) {
				path = TempPath.getTempPath(context);
			} else {
				path = new File(value);
			}
			if (!path.exists()) {
				context.error("Error accessing SQL path (" + path.getAbsolutePath() + ").");
				throw new ExitException();
			}
			final String script = response.get().startsWith("\"") && response.get().endsWith("\"")
					? JsonValue.readFrom(response.get()).asString()
					: response.get();
			final File file = new File(path.getAbsolutePath() + "/sql-migration-" + (new Date().getTime()) + ".sql");
			try {
				Utils.saveFile(file, script);
			} catch (IOException e) {
				context.error("Error saving migration script to " + file.getAbsolutePath());
				context.error(e);
				throw new ExitException();
			}
			context.show("Migration saved to " + file.getAbsolutePath());
			if (script.length() > 0) {
				final String[] descriptions = extractDescriptions(script);
				for (int i = 1; i < descriptions.length; i++) {
					context.log(descriptions[i]);
				}
			} else {
				context.show("No database changes detected.");
			}
			context.cache(MIGRATION_FILE_NAME, file);
		}
	}

	@Override
	public String getShortDescription() {
		return "Create SQL migration from previous DSL to the current one";
	}

	@Override
	public String getDetailedDescription() {
		return "DSL Platform will compare previously applied DSL with the current one and provide a migration SQL script.\n" +
				"Developer can inspect migration (although it contains a lot of boilerplate due to Postgres dependency graph),\n" +
				"to check if the requested migration matches what he had in mind.\n" +
				"Every migration contains description of the important changes to the database.\n" +
				"\n" +
				"Postgres migrations are transactional due to Transactional DDL feature of the Postgres.\n" +
				"\n" +
				"While for most migrations ownership of the database is sufficient, some require superuser access (Enum changes, strange primary keys, ...).";
	}
}
