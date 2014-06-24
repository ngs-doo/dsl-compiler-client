package com.dslplatform.compiler.client.cmdline;

import com.dslplatform.compiler.client.Api;
import com.dslplatform.compiler.client.ApiImpl;
import com.dslplatform.compiler.client.HttpTransportProvider;
import com.dslplatform.compiler.client.api.core.impl.HttpRequestBuilderImpl;
import com.dslplatform.compiler.client.api.core.impl.UnmanagedDSLImpl;
import com.dslplatform.compiler.client.cmdline.parser.Arguments;
import com.dslplatform.compiler.client.diff.HashBodyMapTool;
import com.dslplatform.compiler.client.diff.PathAction;
import com.dslplatform.compiler.client.io.FileLoader;
import com.dslplatform.compiler.client.io.Hash;
import com.dslplatform.compiler.client.io.Output;
import com.dslplatform.compiler.client.io.PrintStreamOutput;
import com.dslplatform.compiler.client.params.*;
import com.dslplatform.compiler.client.response.*;
import org.apache.commons.codec.Charsets;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Class that proxies responses to from the Api to the output and provides user experience in sense of validating chosen actions.
 */
public class ActionDefinition extends ActionContext implements CLCAction {

    private final static String source_compilation_failed_msg = "Source compilation failed ";
    private final static String error_while_writing_sources_msg = "An error occurred while writing sources ";
    private final static String source_generation_failed_msg = "Source generation failed ";
    private final static String no_successfully_generated_sources_msg = "Successfully generated sources ";
    private final static String writing_files_to_msg = "Writing files to ";
    private final static String success_receive_migration_msg = "Successfully received migration sql from remote.";
    private final static String error_writing_migration_msg = "An error occurred while writing migration ";
    private final static String error_getting_sql_migration_msg = "There was an error with get sql migration request.";
    private final static String migration_file_read_failed_msg = "Failed to read migration file";
    private final static String retrying_unmanaged_source_request_msg = "Retrying unmanaged source request";
    private final static String retrying_unmanaged_compilation_request_msg = "Retrying unmanaged source compilation";
    private final static String database_connection_failure_msg = "There was a problem connection to the database ";
    private final static String migration_application_failed_msg = "Migration was not applied successfully";
    private final static String database_upgrade_successful_msg = "Database upgrade successful";

    private final static String correct_DSL_and_try_again_prompt = "Correct DSL and try again";
    private final static String generate_sources_if_DB_failed_continue_prompt = "download sources even thou database upgrade failed.";
    private final static String migration_request_prompt = "Request migration from remote?";
    private final static String apply_changes_to_the_database_prompt = "Apply changes to the database";
    private final static String are_diff_changes_good_prompt = "those changes";
    private final static String retry_get_sources_prompt = "Retry get sources";
    private final static String retry_compilation_prompt = "Retry compilation";

    public ActionDefinition(Logger logger, Arguments arguments) throws IOException {
        this(new ApiImpl(
                        logger,
                        new HttpRequestBuilderImpl(),
                        HttpTransportProvider.httpTransport(),
                        new UnmanagedDSLImpl()
                ),
                logger,
                new PrintStreamOutput(),
                arguments
        );
    }

    public ActionDefinition(Api api, Logger logger, Output output, Arguments arguments) {
        super(api, logger, output, arguments, new SystemInCommandLinePrompt(output), new ClcIO(logger));
    }

    public ActionDefinition(Api api, Logger logger, Output output, Arguments arguments, CommandLinePrompt clp, IO io) {
        super(api, logger, output, arguments, clp, io);
    }

    public boolean parseDSL() {
        DSL dsl = getDSL();
        return parseDSL(dsl);
    }

    private boolean parseDSL(DSL dsl) {
        logger.trace("About to parse DSL");
        ParseDSLResponse parseDSLResponse = api.parseDSL(getToken(), dsl.files);
        if (parseDSLResponse.authorized) output.println(parseDSLResponse.parseMessage);
        else output.println(parseDSLResponse.authorizationErrorMessage);
        return parseDSLResponse.parsed;
    }

    /**
     * Informs a user of the changes made to the DSL.
     */
    public void getChanges() {
        logger.trace("About to get changes.");
        DSL dsl = getDSL();
        DataSource dataSource = getDataSource(); // todo - missing DBAuth parametar
        getChanges(dataSource, dsl);
    }

    public void getChanges(DataSource dataSource, DSL dsl) {
        GetLastUnmanagedDSLResponse getLastUnmanagedDSLResponse = api.getLastUnmanagedDSL(dataSource);
        final Map<String, String> olddsl;
        if (getLastUnmanagedDSLResponse.lastMigration == null) olddsl = DSL.empty().files;
        else olddsl = getLastUnmanagedDSLResponse.lastMigration.dsls;
        String formattedDiff = api.getDiff(olddsl, dsl.files);
        output.print(formattedDiff);
    }

    public void generateSources() {
    } // todo - managed - on hold

    private void generateSources(DSL dsl) {
    } // todo - managed - on hold

    public void unmanagerCSServer() {
    } // todo - ambiguous - on hold

    private void unmanagerCSServer(DSL dsl) {
    } // todo - ambiguous - on hold

    /**
     * Requests for Unmanaged source.
     */
    public boolean unmanagedSource() {
        final DSL dsl = getDSL();
        final OutputPath outputPath = arguments.getOutputPath();
        final PackageName packageName = arguments.getPackageName();
        return unmanagedSource(dsl, packageName, outputPath);
    }

    private boolean unmanagedSource(DSL dsl, PackageName packageName, OutputPath outputPath) {
        Set<String> targetStringSet = mapTargets();
        Set<String> options = mapOptions();
        GenerateUnmanagedSourcesResponse generateUnmanagedSourcesResponse = api.generateUnmanagedSources(getToken(), packageName.packageName, targetStringSet, options, dsl.files);

        if (generateUnmanagedSourcesResponse.generationSuccessful) {
            List<Source> sourceList = generateUnmanagedSourcesResponse.sources;

            output.println(no_successfully_generated_sources_msg + sourceList.size());
            output.println(writing_files_to_msg + outputPath.outputPath.getAbsolutePath());
            try {
                updateFiles(logger, generateUnmanagedSourcesResponse.sources, outputPath.outputPath);
                return true;
            } catch (IOException e) {
                output.println(error_while_writing_sources_msg + e.getMessage());
                logger.error(error_while_writing_sources_msg + e.getMessage());
            }
        } else {
            output.println(source_generation_failed_msg + generateUnmanagedSourcesResponse.authorizationErrorMessage);
            logger.error(source_generation_failed_msg + generateUnmanagedSourcesResponse.authorizationErrorMessage);
        }
        return false;
    }

    public boolean compileCSServer() {
        CompileCSharpServerResponse compileCSharpServerResponse = api.compileCSharpServer(arguments.getOutputPath().outputPath, getRevenjPath(), getTargetPath());
        if (compileCSharpServerResponse.compilationSuccessful) {
            output.println(compileCSharpServerResponse.compilationMessage);
            logger.info(compileCSharpServerResponse.compilationMessage);
            return true;
        } else {
            output.println(source_compilation_failed_msg + compileCSharpServerResponse.compilationMessage);
            logger.error(source_compilation_failed_msg + compileCSharpServerResponse.compilationMessage);
            return false;
        }
    }

    /**
     * Requests a migration based on the last migration in the provided database at the moment, or null if database is new, and the dsl provided in the parameters.
     * Will output migration to a file.
     *
     * @return migration or null if failed.
     */
    public GenerateMigrationSQLResponse sqlMigration() {
        return sqlMigration(getDSL());
    }

    public GenerateMigrationSQLResponse sqlMigration(DSL dsl) {
        GenerateMigrationSQLResponse generateMigrationSQLResponse = api.generateMigrationSQL(getToken(), getDataSource(), dsl.files);
        if (generateMigrationSQLResponse.migrationRequestSuccessful) {
            String migration = generateMigrationSQLResponse.migration;

            output.println(success_receive_migration_msg);
            File migrationOutputPath = getMigrationPath();
            if (migrationOutputPath == null) {
                output.println("Migration path was not specified, skipping writing to disk.");
                logger.info("Migration path was not specified, skipping writing to disk.");
            } else {
                try {
                    output.println("Writing migration to " + migrationOutputPath.getAbsolutePath());
                    logger.info("Writing migration to " + migrationOutputPath.getAbsolutePath());
                    io.write(migrationOutputPath, migration, Charsets.UTF_8);
                } catch (IOException e) {
                    output.println(error_writing_migration_msg + e.getMessage());
                    logger.error(error_writing_migration_msg + e.getMessage());
                    return null;
                }
            }
            return generateMigrationSQLResponse;
        } else {
            output.println(error_getting_sql_migration_msg + generateMigrationSQLResponse.authorizationErrorMessage);
            logger.error(error_getting_sql_migration_msg + generateMigrationSQLResponse.authorizationErrorMessage);
            return clp.promptRetry("Retry request for sql migration") ? sqlMigration(dsl) : null;
        }
    }

    /**
     * Applies a migration sql to the database
     * migrationSQL is read from the disk if existing, otherwise user is prompted to request it.
     */
    public boolean upgradeUnmanagedDatabase() {
        final File migrationPath = getMigrationPath();
        /* see if there is a migration file in path, ask weather to upgrade with it */
        if (migrationPath.exists() && clp.promptContinue(" read migration sql from " + migrationPath + "  and use it to upgrade")) {
            String migrationOrNull = readMigrationFromDisk(migrationPath, logger, output, clp);
            if (migrationOrNull == null) return upgradeUnmanagedDatabase(); /* back to the start */
            else return upgradeUnmanagedDatabase(migrationOrNull);
        } else {
            /* or get the migration from the remote */
            output.println("Upgrading with remotely requested migration");
            logger.info("Upgrading with remotely requested migration");
            GenerateMigrationSQLResponse generateMigrationSQLResponse = sqlMigration();
            return upgradeUnmanagedDatabase(generateMigrationSQLResponse);
        }
    }

    private static String readMigrationFromDisk(File migrationPath, Logger logger, Output output, CommandLinePrompt clp) {
        String migration;
        try {
            migration = FileUtils.readFileToString(migrationPath);
        } catch (IOException e) {
            e.printStackTrace();
            output.println(migration_file_read_failed_msg + e.getMessage());
            return clp.promptRetry("Unable to read file " + migrationPath.getAbsolutePath())
                    ? readMigrationFromDisk(migrationPath, logger, output, clp)
                    : null;
        }
        return migration;
    }

    private boolean upgradeUnmanagedDatabase(String migration) {
        return upgradeUnmanagedDatabase(new GenerateMigrationSQLResponse(migration));
    }

    private boolean upgradeUnmanagedDatabase(GenerateMigrationSQLResponse migration) {
        /* print migration information and prompt to continue if destructive */
        if (arguments.isAllowUnsafe() || !migration.isMigrationDestructive || clp.promptContinue("a destructive migration")) {
            UpgradeUnmanagedDatabaseResponse upgradeUnmanagedDatabaseResponse = api.upgradeUnmanagedDatabase(getDataSource(), Arrays.asList(migration.migration));

            /* be informal */
            if (upgradeUnmanagedDatabaseResponse.databaseConnectionSuccessful) {
                if (upgradeUnmanagedDatabaseResponse.successfulUpgrade) {
                    output.println(database_upgrade_successful_msg);
                    logger.info(database_upgrade_successful_msg);
                    return true;
                } else {
                    output.println(migration_application_failed_msg);
                    logger.error(migration_application_failed_msg);
                    return false;
                }
            } else {
                output.println(database_connection_failure_msg + upgradeUnmanagedDatabaseResponse.databaseConnectionErrorMessage);
                logger.error(database_connection_failure_msg + upgradeUnmanagedDatabaseResponse.databaseConnectionErrorMessage);
                return false;
            }
        } else
            return false;
    }

    /**
     * Aggregation of all tasks will perform following:
     * Parse and diff DSL - display information to user
     * <p>
     * Get Migration SQL - prompt user should it continue in case migration is destructive
     * Apply migration SQL
     * <p>
     * Get CS Sources and compile them.
     * <p>
     * Deploy to mono service.
     */
    public boolean deployUnmanagedServer() {
        DataSource dataSource = getDataSource();
        DSL dsl = getDSL();
        return deployUnmanagedServer(dsl, dataSource);
    }

    private boolean deployUnmanagedServer(DSL dsl, DataSource dataSource) {
        /* 1: Parse DSL, continue if parseable prompt retry if not.
           Optional step 1.1 Show diff prompt continuation.
         */
        if (parseDSL(dsl)) {
            if (!skip_diff) {
                getChanges(dataSource, dsl);
                /* todo - maybe add a 3 way here reload, continue, quit */
                if (!clp.promptContinue(are_diff_changes_good_prompt)) return false;
            }
        } else {
            /* prompt user to fix dsl and try again */
            return (clp.promptRetry(correct_DSL_and_try_again_prompt)) && deployUnmanagedServer();
        }

        /* 2,3 : upgrade database
           2: Show migration information
           3: Apply migration
           */
        if (!upgradeUnmanagedDatabase()) {
            if (!clp.promptContinue(generate_sources_if_DB_failed_continue_prompt)) return false;
        }

        /* 4: Get sources */
        while (!unmanagedSource(dsl, arguments.getPackageName(), arguments.getOutputPath()) && clp.promptRetry(retry_get_sources_prompt)) {
            output.println(retrying_unmanaged_source_request_msg);
            logger.trace(retrying_unmanaged_source_request_msg);
        }

        /* 5: Try, Retry compilation */
        while (!compileCSServer() && clp.promptRetry(retry_compilation_prompt)) {
            output.println(retrying_unmanaged_compilation_request_msg);
            logger.trace(retrying_unmanaged_compilation_request_msg);
        }
        /* 6: Deploy, generated sources - todo */

        return true;
    }

// °º¤ø,¸¸,ø¤º°`°º¤ø, Output Helpers ,ø¤°º¤ø,¸¸,ø¤º°`°º¤ø,¸

    private void updateFiles(
            Logger logger,
            final List<Source> fileBodies,
            final File outputPath) throws IOException {

        if (!outputPath.exists()) {
            outputPath.mkdirs();
        }

        final Set<String> languagePaths = new LinkedHashSet<String>();
        for (final Source source : fileBodies) {
            languagePaths.add(source.path.replaceFirst("/.*", ""));
        }

        final FileLoader fileLoader = new FileLoader();
        for (final String path : languagePaths) {
            final String language = path.replaceFirst("/.*", "");
            final File languagePath = new File(outputPath.getPath(), language);

            if (languagePath.isDirectory()) {
                logger.trace("Marking managed directory: " + languagePath);
                fileLoader.addPath(outputPath.getPath(), languagePath.getPath());
            }
        }

        final Map<Hash, SortedSet<String>> oldHash = fileLoader
                .getHashBodyMap();

        final FileLoader newFilesLoader = new FileLoader();

        for (final Source source : fileBodies) {
            newFilesLoader.addBytes(cleanFilename(source.path),
                    source.content);
        }

        final Map<Hash, SortedSet<String>> newHash = newFilesLoader
                .getHashBodyMap();
        final Map<String, PathAction> actions = HashBodyMapTool.compareHashBodyMaps(oldHash, newHash);
        final SortedMap<String, byte[]> newFiles = newFilesLoader.getBodies();

        for (final Map.Entry<String, PathAction> hashAction : actions
                .entrySet()) {
            final String hash = hashAction.getKey();
            final PathAction action = hashAction.getValue();

            final File source = action.source == null ? null : new File(outputPath,
                    action.source);

            final File destination = action.destination == null
                    ? null
                    : new File(outputPath, action.destination);

            logger.debug("Performing action: " + action.action.name()
                    + " on file " + source
                    + (destination == null ? "" : " -> dest:" + destination));

            switch (action.action) {
                case NO_CHANGE:
                case SKIPPED:
                case CREATED_DIR:
                    break;

                case CREATED:
                case MODIFIED:
                    io.write(source, newFiles.get(hash));
                    break;

                case MOVED:
                    try {
                        io.move(source, destination);
                    } catch (final IOException e) {
                        if (source.getCanonicalPath().equalsIgnoreCase(
                                destination.getCanonicalPath())
                                && System.getProperty("os.name").contains(
                                "Windows")) {
                            logger.debug(String
                                    .format("Could not move \"%s\" to \"%s\" (Windows is case insensitive)",
                                            source, destination));
                        } else {
                            throw e;
                        }
                    }
                    checkIfParentEmptyAndDelete(source, outputPath);
                    break;

                case COPY:
                    io.copy(source, destination);
                    break;

                case DELETED_DIR:
                case DELETED:
                    io.delete(source);
                    checkIfParentEmptyAndDelete(source, outputPath);
                    break;
            }
        }
    }

    private void checkIfParentEmptyAndDelete(
            final File source,
            final File outputPath) {
        final File parent = source.getParentFile();
        if (parent.list().length == 0 && !parent.equals(outputPath)) {
            io.delete(parent);
            checkIfParentEmptyAndDelete(parent, outputPath);
        }
    }

    private static String cleanFilename(final String path) {
        if (path == null) {
            return null;
        }
        if (path.startsWith("/")) {
            return cleanFilename(path.substring(1));
        }
        return path.replace("//", "/");
    }
}

