package com.dslplatform.compiler.client.cmdline;

import com.dslplatform.compiler.client.Api;
import com.dslplatform.compiler.client.ApiImpl;
import com.dslplatform.compiler.client.HttpTransportProvider;
import com.dslplatform.compiler.client.IO;
import com.dslplatform.compiler.client.api.core.impl.HttpRequestBuilderImpl;
import com.dslplatform.compiler.client.api.core.impl.UnmanagedDSLImpl;
import com.dslplatform.compiler.client.cmdline.parser.Arguments;
import com.dslplatform.compiler.client.io.Output;
import com.dslplatform.compiler.client.io.PrintStreamOutput;
import com.dslplatform.compiler.client.params.*;
import com.dslplatform.compiler.client.response.*;
import org.apache.commons.codec.Charsets;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class that proxies responses to from the Api to the output and provides a user experience in a sense of validating chosen actions.
 */
public class ActionDefinition extends ActionContext {

    private final static String successfulManagedUpgrade = "Successful update!";
    private final static String source_compilation_failed_msg = "Source compilation failed ";
    private final static String error_while_writing_sources_msg = "An error occurred while writing sources ";
    private final static String source_generation_failed_msg = "Source generation failed ";
    private final static String no_successfully_generated_sources_msg = "Successfully generated sources ";
    private final static String writing_files_to_msg = "Writing files to ";
    private final static String success_receive_migration_msg = "Successfully received migration sql from remote.";
    private final static String error_writing_migration_msg = "An error occurred while writing migration ";
    private final static String error_getting_sql_migration_msg = "There was an error with get sql migration request.";
    private final static String migration_file_read_failed_msg = "Failed to read migration file";
    private final static String writing_migration_to_msg = "Writing migration to %s";
    private final static String retrying_unmanaged_source_request_msg = "Retrying unmanaged source request";
    private final static String retrying_unmanaged_compilation_request_msg = "Retrying unmanaged source compilation";
    private final static String migration_not_specified_skipping_writing_msg = "Migration path was not specified, skipping writing to disk.";
    private final static String database_connection_failure_msg = "There was a problem connection to the database ";
    private final static String migration_application_failed_msg = "Migration was not applied successfully";
    private final static String database_upgrade_successful_msg = "Database upgrade successful";
    private final static String upgrading_with_requested_migration_msg = "Upgrading with remotely requested migration";
    private final static String database_upgrade_empty_msg = "Migration SQL will make no changes!";
    private final static String missing_mono_location_msg = "Mono Location was not provided!";

    private final static String correct_DSL_and_try_again_prompt = "Correct DSL and try again";
    private final static String generate_sources_if_DB_failed_continue_prompt = "download sources even thou database upgrade failed.";
    private final static String migration_request_prompt = "Request migration from remote?";
    private final static String apply_changes_to_the_database_prompt = "Apply changes to the database";
    private final static String read_and_upgrade_prompt = "read migration sql from %s and use it to upgrade the database";
    private final static String unable_to_read_file_retry_prompt = "Unable to read file %s";
    private final static String destructive_migration_prompt = "a destructive migration";
    private final static String are_diff_changes_good_raw_prompt = "Do you wish to continue with this changes, quit or reload?";
    private final static String continue_with_destructive_upgrade = "Would you like to continue with a destructive upgrade?";
    private final static String retry_sql_migration_request = "Retry request for sql migration";
    private final static String retry_get_sources_prompt = "Retry get sources";
    private final static String retry_compilation_prompt = "Retry compilation";

    public ActionDefinition(final Logger logger, final Arguments arguments) throws IOException {
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

    public ActionDefinition(final Api api, final Logger logger, final Output output, final Arguments arguments) {
        super(api, logger, output, arguments, new SystemInCommandLinePrompt(output));
    }

    public ActionDefinition(final Api api, final Logger logger, final Output output, final Arguments arguments, final CommandLinePrompt clp) {
        super(api, logger, output, arguments, clp);
    }

    public boolean parseDSL() {
        return parseDSL(getDSL());
    }

    private boolean parseDSL(final DSL dsl) {
        logger.trace("About to parse DSL");
        final ParseDSLResponse parseDSLResponse = api.parseDSL(getToken(), dsl.files);
        if (parseDSLResponse.authorized) output.println(parseDSLResponse.parseMessage);
        else output.println(parseDSLResponse.authorizationErrorMessage);
        return parseDSLResponse.parsed;
    }

    @Override
    public void lastDSL() {

    }

    /**
     * Informs a user of the changes made to the DSL.
     */
    public void getChanges() {
        logger.trace("About to get changes.");
        getChanges(getDSL());
    }

    /* For diffing. */
    private void getChanges(final DSL dsl) {
        /* if managed get last dsl from remote, if unmanaged query it from the database, using api */
        if (arguments.isManaged()) {
            final GetLastManagedDSLResponse getLastManagedDSLResponse = api.getLastManagedDSL(getToken(), arguments.getProjectID().projectID);
            getChanges(getLastManagedDSLResponse.dsls, dsl.files);
        } else {
            final GetLastUnmanagedDSLResponse getLastUnmanagedDSLResponse = api.getLastUnmanagedDSL(getDataSource());
            final Map<String, String> oldDsl;
            if (getLastUnmanagedDSLResponse.lastMigration == null) oldDsl = DSL.empty().files;
            else oldDsl = getLastUnmanagedDSLResponse.lastMigration.dsls;
            getChanges(oldDsl, dsl.files);
        }
    }

    private void getChanges(final Map<String, String> oldDsl, final Map<String, String> newDsl) {
        final String formattedDiff = api.getDiff(oldDsl, newDsl);
        output.print(formattedDiff);
    }

    /**
     * Parse Diff procedure interacting with the user
     *
     * @param dsl
     * @return Users choice on continuation with procedure
     */
    private ContinueRetryQuit parseDiff(DSL dsl) {
        if (parseDSL(dsl)) {
            if (!skip_diff) {
                getChanges(dsl);
                /* prompt user to continue, retry (reload DSL) or quit. */
                return prompt.promptCRQ(are_diff_changes_good_raw_prompt);
            }
            return ContinueRetryQuit.Continue;
        } else {
            /* Prompt user to fix dsl and try again. */
            return (prompt.promptRetry(correct_DSL_and_try_again_prompt)) ?
                    ContinueRetryQuit.Retry : ContinueRetryQuit.Quit;
        }
    }

    @Override
    public boolean upgrade() {
        final Set<String> targetStringSet = mapTargets();

        final Set<String> options = mapOptions();
        final String migration_unsafe = (arguments.isAllowUnsafe()) ? "unsafe" : null;
        final DSL dsl = getDSL();
        switch (parseDiff(dsl)) {
            case Retry:
                return upgrade();
            case Quit:
                return false;
        }
        final UpdateManagedProjectResponse updateManagedProjectResponse = api.updateManagedProject(getToken(), arguments.getProjectID().projectID, targetStringSet, arguments.getPackageName().packageName, migration_unsafe, options, dsl.files);

        if (!updateManagedProjectResponse.authorized) {
            output.println(updateManagedProjectResponse.authorizationErrorMessage);
            return false;
        } else {
            if (updateManagedProjectResponse.updateSuccessful) {
                output.print(successfulManagedUpgrade);
                return true;
            } else {
                output.print(updateManagedProjectResponse.unsuccessfulUpdateMessage);
                switch (prompt.promptCRQ(continue_with_destructive_upgrade)) {
                    case Retry:
                        return upgrade();
                    case Quit:
                        return false;
                }
                final String user_chosen_migration_unsafe = "unsafe";
                final UpdateManagedProjectResponse updateUnsafeManagedProjectResponse =
                        api.updateManagedProject(getToken(), arguments.getProjectID().projectID, targetStringSet, arguments.getPackageName().packageName, user_chosen_migration_unsafe, options, dsl.files);
                if (updateUnsafeManagedProjectResponse.updateSuccessful) {
                    output.println(successfulManagedUpgrade);
                } else {
                    output.println(updateUnsafeManagedProjectResponse.unsuccessfulUpdateMessage);
                }
                return true;
            }
        }
    }

    /**
     * Generates client source for connecting to the managed revenj instance.
     *
     * @return true if successful.
     */
    public boolean generateSources() {
        final Set<String> targetStringSet = mapTargets();
        final Set<String> options = mapOptions();
        final GenerateSourcesResponse generateSourcesResponse = api.generateSources(getToken(), arguments.getProjectID().projectID, targetStringSet, arguments.getPackageName().packageName, options);

        return writeGenerateSourcesResponseToOutputPath(generateSourcesResponse, arguments.getOutputPath());
    }

    /**
     * Requests for the unmanaged source.
     */
    public boolean unmanagedSource() {
        return unmanagedSource(getDSL(), arguments.getPackageName(), arguments.getOutputPath());
    }

    private boolean unmanagedSource(final DSL dsl, final PackageName packageName, final OutputPath outputPath) {
        final Set<String> targetStringSet = mapTargets();
        final Set<String> options = mapOptions();
        final GenerateSourcesResponse generateSourcesResponse = api.generateUnmanagedSources(getToken(), packageName.packageName, targetStringSet, options, dsl.files);
        return writeGenerateSourcesResponseToOutputPath(generateSourcesResponse, outputPath);
    }

    public boolean compileCSServer() {
        /* pre 5: check for revenj */
        while (!getRevenj() && prompt.promptRetry("Do you wish to retry fetching revenj?")) {
            output.println("Retrying fetching revenj!");
            logger.info("Retrying fetching revenj!");
        }
        final CompileCSharpServerResponse compileCSharpServerResponse = api.compileCSharpServer(arguments.getOutputPath().outputPath, getRevenjPath(), getTargetPath());
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
     * Requests a migration based on the last migration in the provided database at the moment, or null if the database is new, and the dsl provided in the parameters.
     * Will output the migration to the file.
     *
     * @return migration or null if failed.
     */
    public GenerateMigrationSQLResponse sqlMigration() {
        return sqlMigration(getDSL());
    }

    public GenerateMigrationSQLResponse sqlMigration(final DSL dsl) {
        final GenerateMigrationSQLResponse generateMigrationSQLResponse = api.generateMigrationSQL(getToken(), getDataSource(), dsl.files);
        if (generateMigrationSQLResponse.migrationRequestSuccessful) {
            final String migration = generateMigrationSQLResponse.migration;

            output.println(success_receive_migration_msg);
            final File migrationOutputPath = getMigrationPath();
            if (migrationOutputPath == null) {
                output.println(migration_not_specified_skipping_writing_msg);
                logger.info(migration_not_specified_skipping_writing_msg);
            } else {
                try {
                    final String formatted_migration_write_information = String.format(writing_migration_to_msg, migrationOutputPath.getAbsolutePath());
                    output.println(formatted_migration_write_information);
                    logger.info(formatted_migration_write_information);
                    IO.write(migrationOutputPath, migration, Charsets.UTF_8);
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
            return prompt.promptRetry(retry_sql_migration_request) ? sqlMigration(dsl) : null;
        }
    }

    /**
     * Applies a migration sql to the database
     * migrationSQL is read from the disk if existing, otherwise user is prompted should the client request for it.
     */
    public boolean upgradeUnmanagedDatabase() {
        /* Get the migration from the remote */
        output.println(upgrading_with_requested_migration_msg);
        logger.info(upgrading_with_requested_migration_msg);
        final GenerateMigrationSQLResponse generateMigrationSQLResponse = sqlMigration();
        return upgradeUnmanagedDatabase(generateMigrationSQLResponse, getDataSource());
    }

    private boolean upgradeUnmanagedDatabase(final GenerateMigrationSQLResponse migration, final DataSource dataSource) {
        /* print migration information and, if its destructive, prompt user, then continue  */

        if (migration.migration.isEmpty()) {
            output.println(database_upgrade_empty_msg);
            logger.info(database_upgrade_empty_msg);
            return true;
        } else if (arguments.isAllowUnsafe() || !migration.isMigrationDestructive || prompt.promptContinue(destructive_migration_prompt)) {
            final UpgradeUnmanagedDatabaseResponse upgradeUnmanagedDatabaseResponse = api.upgradeUnmanagedDatabase(dataSource, Arrays.asList(migration.migration));

            /* return true if success, false if upgrade failed and be informal */
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
        final DSL dsl = getDSL();

        /* 1: Parse DSL, continue if parseable prompt retry if not.
           Optional step 1.1 Show diff prompt continuation.
         */
        switch (parseDiff(dsl)) {
            case Retry:
                return deployUnmanagedServer();
            case Quit:
                return false;
        }

        /* 2, 3: Upgrade database.
           2: Show migration information
           3: Apply migration
           */
        if (!upgradeUnmanagedDatabase()) {
            /* Something went wrong upgrading the database, prompt user if (s)he wishes to continue fetching the sources and to optionally compile them. */
            if (!prompt.promptContinue(generate_sources_if_DB_failed_continue_prompt)) return false;
        }

        /* 4: Get sources. */
        while (!unmanagedSource(dsl, arguments.getPackageName(), arguments.getOutputPath()) && prompt.promptRetry(retry_get_sources_prompt)) {
            output.println(retrying_unmanaged_source_request_msg);
            logger.trace(retrying_unmanaged_source_request_msg);
        }

        /* 5: Try, Retry compilation... */
        while (!compileCSServer() && prompt.promptRetry(retry_compilation_prompt)) {
            output.println(retrying_unmanaged_compilation_request_msg);
            logger.trace(retrying_unmanaged_compilation_request_msg);
        }

        /* 6: Deploy generated sources, actually just copy them */
        final MonoApplicationPath monoApplicationPath = arguments.getMonoApplicationPath();
        if (monoApplicationPath != null) {
            api.makeMonoServer(monoApplicationPath, arguments.getRevenjPath(), arguments.getCompilationTargetPath(), getConnectionString());
        } else {
            output.println(missing_mono_location_msg);
        }

        return true;
    }

    /* °º¤ø,¸¸,ø¤º°`°º¤ø, Output Helpers ,ø¤°º¤ø,¸¸,ø¤º°`°º¤ø,¸ */

    protected boolean writeGenerateSourcesResponseToOutputPath(final GenerateSourcesResponse generateSourcesResponse, final OutputPath outputPath) {
        if (generateSourcesResponse.generatedSuccess) {
            final List<Source> sourceList = generateSourcesResponse.sources;

            output.println(no_successfully_generated_sources_msg + sourceList.size());
            output.println(writing_files_to_msg + outputPath.outputPath.getAbsolutePath());
            try {
                IO.updateFiles(logger, generateSourcesResponse.sources, outputPath.outputPath);
                return true;
            } catch (IOException e) {
                output.println(error_while_writing_sources_msg + e.getMessage());
                logger.error(error_while_writing_sources_msg + e.getMessage());
                return false;
            }
        } else {
            output.println(source_generation_failed_msg + generateSourcesResponse.authorizationErrorMessage);
            logger.error(source_generation_failed_msg + generateSourcesResponse.authorizationErrorMessage);
            return false;
        }
    }

    private boolean getRevenj() {
        final RevenjPath revenjPath = arguments.getRevenjPath();
        /* if its already present at a given location just return true */
        if (revenjPath.revenjPath.exists()) {
            logger.info("Revenj exists at {}", revenjPath.revenjPath);
            return true;
        } else {
            if (prompt.promptContinue(String.format("Download version %s of revenj.", revenjPath.revenjPath))) {
                final CacheRevenjResponse cacheRevenjResponse = api.cacheRevenj(arguments.getRevenjVersion(), revenjPath);
                output.print(cacheRevenjResponse.message);
                return cacheRevenjResponse.success;
            }
            else
                return false;
        }
    }

}
