package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.*;
import com.dslplatform.compiler.client.json.JsonObject;
import com.dslplatform.compiler.client.json.JsonValue;

import java.io.File;
import java.io.IOException;
import java.util.*;

public enum Migration implements CompileParameter {
	INSTANCE;

	@Override
	public boolean check(final Map<InputParameter, String> parameters) {
		if (parameters.containsKey(InputParameter.MIGRATION)) {
			if(!parameters.containsKey(InputParameter.CONNECTION_STRING)) {
				System.out.println("Connection string is required to create a migration script");
				System.exit(0);
			}
			if(parameters.containsKey(InputParameter.SQL)) {
				final String value = parameters.get(InputParameter.SQL);
				if (value == null || value.length() == 0) {
					return true;
				}
				final File sqlPath = new File(value);
				if (!sqlPath.exists()) {
					System.out.println("Path for SQL migration script provided (" + value + ") but not found");
					return false;
				}
			}
		}
		return true;
	}

	private static File migrationFile;

	public static File getMigrationFile() {
		return migrationFile;
	}

	@Override
	public void run(final Map<InputParameter, String> parameters) {
		if (parameters.containsKey(InputParameter.MIGRATION)) {
			final Map<String, String> currentDsl = DslPath.getCurrentDsl(parameters);
			final Map.Entry<Map<String, String>, String> previousDslAndVersion = DbConnection.getDatabaseDslAndVersion(parameters);
			final String url =  "Platform.svc/unmanaged/postgres-migration?version=" + previousDslAndVersion.getValue();
			final JsonObject arg =
					new JsonObject()
							.add("Old", Utils.toJson(previousDslAndVersion.getKey()))
							.add("New", Utils.toJson(currentDsl));
			final Either<String> response = DslServer.put(url.toString(), parameters, arg);
			if (!response.isSuccess()) {
				System.out.println("Error creating SQL migration:");
				System.out.println(response.whyNot());
				System.exit(0);
			}
			final String value = parameters.get(InputParameter.SQL);
			final Either<File> temp = Utils.getOrCreateTempPath();
			final File path;
			if (!parameters.containsKey(InputParameter.SQL) || value == null || value.length() == 0) {
				if (!temp.isSuccess()) {
					System.out.println("Error creating SQL migration to temporary path.");
					System.out.println(response.whyNot());
					System.exit(0);
				}
				path = temp.get();
			} else {
				path = new File(value);
			}
			if (!path.exists()) {
				System.out.println("Error accessing SQL path (" + path.getAbsolutePath() + ").");
				System.exit(0);
			}
			final String script = response.get().startsWith("\"") && response.get().endsWith("\"")
					? JsonValue.readFrom(response.get()).asString()
					: response.get();
			final File file = new File(path.getAbsolutePath() + "/sql-migration-" + (new Date().getTime()) + ".sql");
			try {
				Utils.saveFile(file, script);
			} catch (IOException e) {
				System.out.println("Error saving migration script to " + file.getAbsolutePath());
				System.out.println(e.getMessage());
				System.exit(0);
			}
			migrationFile = file;
		}
	}

	@Override
	public String getShortDescription() {
		return "Create SQL migration from previous DSL to current one";
	}

	@Override
	public String getDetailedDescription() {
		return null;
	}
}
