package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

public enum Migration implements CompileParameter, ParameterParser {
	INSTANCE;

	@Override
	public String getAlias() {
		return "migration";
	}

	@Override
	public String getUsage() {
		return null;
	}

	private final static String DESCRIPTION_START = "/*MIGRATION_DESCRIPTION";
	private final static String DESCRIPTION_END = "MIGRATION_DESCRIPTION*/";

	private static final String POSTGRES_MIGRATION_FILE_NAME = "postgres_migration_file";
	private static final String ORACLE_MIGRATION_FILE_NAME = "oracle_migration_file";

	public static File getPostgresMigrationFile(final Context context) {
		return context.load(POSTGRES_MIGRATION_FILE_NAME);
	}

	public static File getOracleMigrationFile(final Context context) {
		return context.load(ORACLE_MIGRATION_FILE_NAME);
	}

	public static String[] extractDescriptions(final String sql) {
		final int start = sql.indexOf(DESCRIPTION_START);
		final int end = sql.indexOf(DESCRIPTION_END);
		if (end > start) {
			return sql.substring(start + DESCRIPTION_START.length(), end).split("\n");
		}
		return new String[0];
	}

	@Override
	public Either<Boolean> tryParse(final String name, final String value, final Context context) {
		if ("migration".equals(name)) {
			context.put(name, value == null || value.length() == 0 ? null : value);
			return Either.success(true);
		} else {
			for (final String db : new String[]{"postgres", "oracle"}) {
				if (("sql:" + db).equalsIgnoreCase(name)) {
					if (value == null || value.length() == 0) {
						return Either.fail("Custom output file parameter detected, but it's missing file name as an argument. Parameter: " + name);
					}
					final File path = new File(value);
					if (path.exists() && path.isDirectory()) {
						return Either.fail("Output path found, but it's a directory. Parameter: " + name);
					}
					context.put("sql:" + db, value);
					return Either.success(true);
				} else if (name.startsWith("previous-sql:" + db)) {
					if (value == null || value.length() == 0) {
						return Either.fail("Previous sql file parameter detected, but it's missing path as an argument. Parameter: " + name);
					}
					final File previous = new File(value);
					if (!previous.exists()) {
						return Either.fail("Previous sql path provided, but file does not exist at: " + previous.getAbsolutePath() + ". Parameter: " + name);
					} else if (previous.isDirectory()) {
						return Either.fail("Previous sql path found, but it's a directory: " + previous.getAbsolutePath() + ". Parameter: " + name);
					}
					final Either<String> content = Utils.readFile(previous);
					if (!content.isSuccess()) {
						return Either.fail("Unable to read previous sql file from: " + previous.getAbsolutePath() + ". Parameter: " + name);
					}
					context.cache("previous-sql:" + db, content.get());
					context.cache("db-version:" + db, name.substring("previous-sql:".length() + db.length()));
					return Either.success(true);
				}
			}
		}
		return Either.success(false);
	}

	@Override
	public boolean check(final Context context) {
		if (context.contains(INSTANCE)) {
			if (!context.contains(PostgresConnection.INSTANCE)
					&& !context.contains(OracleConnection.INSTANCE)
					&& context.load("previous-sql:postgres") == null
					&& context.load("previous-sql:oracle") == null) {
				context.error("Connection string is required to create a migration script.\n"
						+ "Neither Oracle or Postgres connection string found");
				return false;
			}
			if (context.contains(SqlPath.INSTANCE)) {
				final String value = context.get(SqlPath.INSTANCE);
				if (value == null || value.length() == 0) {
					return true;
				}
				final File sqlPath = new File(value);
				if (!sqlPath.exists()) {
					context.error("Path for SQL migration script provided (" + value + ") but not found");
					return false;
				}
				if (sqlPath.isFile()) {
					context.error("Provided path for SQL migration is a file and not a folder (" + value + ").\n"
							+ "Please specify folder which will be used for migration.");
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public void run(final Context context) throws ExitException {
		if (context.contains(Migration.INSTANCE)) {
			final String value = context.get(SqlPath.INSTANCE);
			final File path;
			if (!context.contains(SqlPath.INSTANCE) || value == null || value.length() == 0) {
				path = TempPath.getTempProjectPath(context);
			} else {
				path = new File(value);
			}
			if (!path.exists()) {
				context.error("Error accessing SQL path (" + path.getAbsolutePath() + ").");
				throw new ExitException();
			}
			if (context.load("previous-sql:postgres") instanceof String || context.contains(PostgresConnection.INSTANCE)) {
				final DatabaseInfo dbInfo = PostgresConnection.getDatabaseDslAndVersion(context);
				createMigration(context, path, dbInfo, POSTGRES_MIGRATION_FILE_NAME);
			}
			if (context.load("previous-sql:oracle") instanceof String || context.contains(OracleConnection.INSTANCE)) {
				final DatabaseInfo dbInfo = OracleConnection.getDatabaseDslAndVersion(context);
				createMigration(context, path, dbInfo, ORACLE_MIGRATION_FILE_NAME);
			}
		}
	}

	public static DatabaseInfo getDatabaseInfo(Context context) throws ExitException {
		if (context.load("previous-sql:postgres") instanceof String || context.contains(PostgresConnection.INSTANCE)) {
			return PostgresConnection.getDatabaseDslAndVersion(context);
		} else if (context.load("previous-sql:oracle") instanceof String || context.contains(OracleConnection.INSTANCE)) {
			return OracleConnection.getDatabaseDslAndVersion(context);
		} else {
			return null;
		}
	}

	private static void createMigration(
			final Context context,
			final File path,
			final DatabaseInfo dbInfo,
			final String file) throws ExitException {
		final List<File> currentDsl = DslPath.getDslPaths(context);
		context.show("Creating SQL migration for " + dbInfo.database + " ...");
		final long start = new Date().getTime();
		final Either<String> migration = DslCompiler.migration(context, dbInfo, currentDsl);
		if (!migration.isSuccess()) {
			context.error("Error creating SQL migration:");
			context.error(migration.whyNot());
			throw new ExitException();
		}
		final long end = new Date().getTime();
		context.show("Running the migration took " + (end - start) / 1000 + " second(s)");
		final String script = migration.get();
		final String customFile = context.get("sql:" + dbInfo.database.toLowerCase());
		final String sqlFileName = customFile != null ? customFile : dbInfo.database.toLowerCase() + "-sql-migration-" + end + ".sql";
		final File sqlFile = new File(path.getAbsolutePath(), sqlFileName);
		boolean isContentSame = false;
		if (customFile != null && sqlFile.exists()) {
			Either<String> content = Utils.readFile(sqlFile);
			isContentSame = content.isSuccess() && content.get().equals(script);
			if (!content.isSuccess() || !isContentSame) {
				if (context.contains(Force.INSTANCE)) {
					context.show("Existing sql file (" + sqlFile.getAbsolutePath() + ") will be overwritten due to force option.");
				} else if (!context.canInteract()) {
					context.error("Custom sql migration file detected at: " + sqlFile.getAbsolutePath() + ". Enable force option, provide a different file name or delete the file for automatic migration");
					throw new ExitException();
				}
				final String answer = context.ask("Existing sql migration file detected at: " + sqlFile.getAbsolutePath() + ". Do you wish to overwrite (y/N):");
				if (!"y".equalsIgnoreCase(answer)) {
					throw new ExitException();
				}
			}
		}
		if (script.length() > 0) {
			if (!isContentSame) {
				try {
					Utils.saveFile(context, sqlFile, script);
				} catch (IOException e) {
					context.error("Error saving migration script to " + sqlFile.getAbsolutePath());
					context.error(e);
					throw new ExitException();
				}
				context.show("Migration saved to " + sqlFile.getAbsolutePath());
			} else {
				context.show("Sql migration remains same as before in: " + sqlFile.getAbsolutePath());
			}
			final String[] descriptions = extractDescriptions(script);
			for (int i = 1; i < descriptions.length; i++) {
				context.log(descriptions[i]);
			}
			context.cache(file, sqlFile);
		} else {
			context.show("No database changes detected.");
			context.cache(file, new File("empty.sql"));
		}
	}

	@Override
	public String getShortDescription() {
		return "Create SQL migration from previous DSL to the current one";
	}

	@Override
	public String getDetailedDescription() {
		return "DSL Platform will compare previously applied DSL with the current one and provide a migration SQL script.\n" +
				"Developer can inspect migration (although it contains a lot of boilerplate due to dependency graph rebuild),\n" +
				"to check if the requested migration matches what he had in mind.\n" +
				"Every migration contains description of the important changes to the database.\n\n" +
				"Postgres migrations are transactional due to Transactional DDL Postgres feature.\n\n" +
				"While for most migrations ownership of the database is sufficient, some require superuser access (Enum changes, strange primary keys, ...).\n\n" +
				"Custom sql files can be specified via sql:[database] file, eg. sql:postgres=03-dsl-migration.sql.\n\n" +
				"To avoid using database, previous sql file can be specified via previous-sql:[databaseVersion] file, eg. previous-sql:postgres9.6=02-dsl-migration.sql.\n\n";
	}
}
