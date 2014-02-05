package com.dslplatform.compiler.client.cmdline.params;

import java.io.IOException;

import com.dslplatform.compiler.client.api.commons.io.IOUtils;
import com.dslplatform.compiler.client.api.params.ArgumentsValidator;
import com.dslplatform.compiler.client.io.Logger;
import com.dslplatform.compiler.client.io.Output;

public class ArgumentsParser extends ArgumentsValidator {
    private final Logger logger;
    private final Output output;

    public ArgumentsParser(
            final Logger logger,
            final Output output,
            final String[] args) throws IOException {
        super(logger);
        this.logger = logger;
        this.output = output;
        parseArguments(args);
    }

    public void parseArguments(final String[] args) throws IOException {
        if (args.length == 0) {
            logger.trace("There were no arguments provided");
            exitWithHelp(false);
        }

        logger.debug(String.format("There were %d arguments provided",
                args.length));

        logger.trace("Running preliminary scan for the help switch");
        for (final String arg : args) {
            if (is(arg, "--")) {
                logger.trace("Stopping preliminary parser (encountered '--').");
                break;
            }

            if (is(arg, "-h", "--help")) {
                logger.trace("Found help switch, rendering help and exiting");
                exitWithHelp(true);
            }
        }

        for (int index = 0; index < args.length; index++) {
            final String arg = args[index];
            final boolean last = index == args.length - 1;

            if (is(arg, "--")) {
                logger.debug("Stopping parser (encountered '--').");
                break;
            }

            // skipping diff can only be disabled (enabled by default)
            if (is(arg, "--skip-diff")) {
                logger.trace("Parsed --skip-diff parameter, setting skip diff to 'true'");
                setSkipDiff(true);
                continue;
            }

            // confirming unsafe operations will not be required
            if (is(arg, "--confirm-unsafe")) {
                logger.trace("Parsed --confirm-unsafe parameter, setting confirm unsafe required to 'false'");
                setConfirmUnsafeRequired(false);
                continue;
            }

            // compile source entities will additionally use the active record pattern
            if (is(arg, "--with-active-record")) {
                logger.trace("Parsed --with-active-record, setting active record option to 'true'");
                setWithActiveRecord(true);
                continue;
            }

            // ---------------------------------------------------------------------------------------------------------

            {   // parse username, new arguments overwrite old ones
                String username = startsWith(arg, "-u", "--username=");
                if (username != null) {
                    logger.trace("Parsed username parameter, overwriting old username: "
                            + username);
                    if (username.isEmpty()) {
                        if (last || (username = args[++index]).isEmpty()) {
                            throw new IllegalArgumentException(
                                    "Username cannot be empty!");
                        }
                        logger.trace("Username argument was empty, read next argument: "
                                + username);
                    }
                    setUsername(username);
                    continue;
                }
            }

            {   // parse password, new arguments overwrite old ones
                String password = startsWith(arg, "-p", "--password=");
                if (password != null) {
                    logger.trace("Parsed password parameter, overwriting old password: ****");
                    if (password.isEmpty()) {
                        if (last || (password = args[++index]).isEmpty()) {
                            throw new IllegalArgumentException(
                                    "Password cannot be empty!");
                        }
                        logger.trace("Password argument was empty, reading next argument: ****");
                    }
                    setPassword(password);
                    continue;
                }
            }

            {   // parse projectID, new arguments overwrite old ones
                String projectID = startsWith(arg, "-i", "--project-id=");
                if (projectID != null) {
                    logger.trace("Parsed project ID parameter, overwriting old project ID: "
                            + projectID);
                    if (projectID.isEmpty()) {
                        if (last || (projectID = args[++index]).isEmpty()) {
                            throw new IllegalArgumentException(
                                    "Project ID cannot be empty!");
                        }
                        logger.trace("Project ID argument was empty, reading next argument: "
                                + projectID);
                    }
                    setProjectID(projectID);
                    continue;
                }
            }

            // ---------------------------------------------------------------------------------------------------------

            {   // parse language, new arguments are joined with old ones (multiple languages supported)
                String languages = startsWith(arg, "-l", "--language=");
                if (languages != null) {
                    logger.trace("Parsed language parameter, adding languages to the list: "
                            + languages);
                    if (languages.isEmpty()) {
                        if (last || (languages = args[++index]).isEmpty()) {
                            throw new IllegalArgumentException(
                                    "Language cannot be empty!");
                        }
                        logger.trace("Language argument was empty, reading next argument: "
                                + languages);
                    }
                    addLanguages(languages);
                    continue;
                }
            }

            {   // parse package name (namespace), new arguments overwrite old ones
                String packageName = startsWith(arg, "-n", "--package-name=");
                if (packageName != null) {
                    logger.trace("Parsed package name parameter, overwriting old package name: "
                            + packageName);
                    if (packageName.isEmpty()) {
                        if (!last) {
                            packageName = args[++index];
                            logger.trace("Package name argument was empty, reading next argument: "
                                    + packageName);
                            if (packageName.isEmpty()) {
                                logger.debug("Files will be compiled with a default package name");
                            }
                        }
                    }
                    setPackageName(packageName);
                    continue;
                }
            }

            {   // parse project name (nick), new arguments overwrite old ones
                String projectName = startsWith(arg, "-k", "--project-name=");
                if (projectName != null) {
                    logger.trace("Parsed project name parameter, overwriting old project name: "
                            + projectName);
                    if (projectName.isEmpty()) {
                        if (!last) {
                            projectName = args[++index];
                            logger.trace("Project name argument was empty, reading next argument: "
                                    + projectName);
                            if (projectName.isEmpty()) {
                                logger.debug("Your new project name will be generated.");
                            }
                        }
                    }
                    setProjectName(projectName);
                    continue;
                }
            }

            // ---------------------------------------------------------------------------------------------------------

            {   // parse DSL path, new arguments are joined with old ones (multiple folders)
                String dslPath = startsWith(arg, "-d", "--dsl-path=");
                if (dslPath != null) {
                    logger.trace("Parsed DSL path parameter, adding DSL path to the list: "
                            + dslPath);
                    if (dslPath.isEmpty()) {
                        if (last || (dslPath = args[++index]).isEmpty()) {
                            throw new IllegalArgumentException(
                                    "Missing DSL path argument!");
                        }
                        logger.trace("Dsl path was empty, reading next argument: "
                                + dslPath);
                    }
                    addDslPath(dslPath);
                    continue;
                }
            }

            {   // parse output path, new arguments overwrite old ones
                String outputPath = startsWith(arg, "-o", "--output-path=");
                if (outputPath != null) {
                    logger.trace("Parsed output path parameter, overwriting old output path: "
                            + outputPath);
                    if (outputPath.isEmpty()) {
                        if (last || (outputPath = args[++index]).isEmpty()) {
                            throw new IllegalArgumentException(
                                    "Missing output path argument!");
                        }
                        logger.trace("Output path was empty, reading next argument: "
                                + outputPath);
                    }
                    setOutputPath(outputPath);
                    continue;
                }
            }

            {   // parse cache path, new arguments overwrite old ones
                String cachePath = startsWith(arg, "-o", "--cache-path=");
                if (cachePath != null) {
                    logger.trace("Parsed cache path parameter, overwriting old cache path: "
                            + cachePath);
                    if (cachePath.isEmpty()) {
                        if (last || (cachePath = args[++index]).isEmpty()) {
                            throw new IllegalArgumentException(
                                    "Missing cache path argument!");
                        }
                        logger.trace("Cache path was empty, reading next argument: "
                                + cachePath);
                    }
                    setCachePath(cachePath);
                    continue;
                }
            }

            {   // parse logging level, new arguments overwrite old ones
                String loggingLevel = startsWith(arg, "-v", "--logging-level=");
                if (loggingLevel != null) {
                    logger.trace("Parsed logging level parameter, overwriting old logging level: "
                            + loggingLevel);
                    if (loggingLevel.isEmpty()) {
                        if (last || (loggingLevel = args[++index]).isEmpty()) {
                            throw new IllegalArgumentException(
                                    "Missing logging level argument!");
                        }
                        logger.trace("Cache path was empty, reading next argument: "
                                + loggingLevel);
                    }
                    setLoggingLevel(loggingLevel);
                    continue;
                }
            }

            // =========================================================================================================

            {   // parse project ini, immediately expanded with new .ini arguments overwriting old ones
                String projectIniPath = startsWith(arg, "-f",
                        "--project-ini-path=");
                if (projectIniPath != null) {
                    logger.trace("Parsed project ini path parameter, processing: "
                            + projectIniPath);
                    if (projectIniPath.isEmpty()) {
                        if (last || (projectIniPath = args[++index]).isEmpty()) {
                            throw new IllegalArgumentException(
                                    "Missing project ini path argument!");
                        }
                        logger.trace("Project ini path was empty, reading next argument: "
                                + projectIniPath);
                    }
                    setProjectIniPath(projectIniPath);
                    continue;
                }
            }

            // =========================================================================================================

            {   // parse project ini, immediately expanded with new .ini arguments overwriting old ones
                String newProjectIniPath = startsWith(arg, "-b",
                        "--new-project-ini-path=");
                if (newProjectIniPath != null) {
                    logger.trace("Parsed path parameter for new project ini, processing: "
                            + newProjectIniPath);
                    if (newProjectIniPath.isEmpty()) {
                        if (last
                                || (newProjectIniPath = args[++index])
                                        .isEmpty()) {
                            throw new IllegalArgumentException(
                                    "Missing new project ini path argument!");
                        }
                        logger.trace("New project ini path was empty, reading next argument: "
                                + newProjectIniPath);
                    }
                    setNewProjectIniPath(newProjectIniPath);
                    continue;
                }
            }

            {   // parse path where to place the server archive file
                String serverArchivePath = startsWith(arg, "-s",
                        "--server-archive-path=");
                if (serverArchivePath != null) {
                    logger.trace("Parsed path parameter for the server archive, processing: "
                            + serverArchivePath);
                    if (serverArchivePath.isEmpty()) {
                        if (last
                                || (serverArchivePath = args[++index])
                                        .isEmpty()) {
                            throw new IllegalArgumentException(
                                    "Missing server archive path argument!");
                        }
                        logger.trace("Server archive path was empty, reading next argument: "
                                + serverArchivePath);
                    }
                    setServerArchivePath(serverArchivePath);
                    continue;
                }
            }

            {   // parse path where to place the server model file
                String serverModelPath = startsWith(arg, "-m",
                        "--server-model-path=");
                if (serverModelPath != null) {
                    logger.trace("Parsed path parameter for the server model, processing: "
                            + serverModelPath);
                    if (serverModelPath.isEmpty()) {
                        if (last
                                || (serverModelPath = args[++index])
                                        .isEmpty()) {
                            throw new IllegalArgumentException(
                                    "Missing server model path argument!");
                        }
                        logger.trace("Server model path was empty, reading next argument: "
                                + serverModelPath);
                    }
                    setServerModelPath(serverModelPath);
                    continue;
                }
            }

            // =========================================================================================================

            // parse action, new arguments are joined with old ones (specific combinations of multiple actions are allowed)
            logger.trace("No specific handler has been found, assuming that this parameter is an action: "
                    + arg);
            addActions(arg);
        }
    }

    // =================================================================================================================

    private static String startsWith(
            final String what,
            final String... switches) {
        final String trimmed = what.trim();
        for (final String sw : switches) {
            if (trimmed.startsWith(sw)) {
                return what.substring(sw.length());
            }
        }
        return null;
    }

    private static boolean is(final String what, final String... switches) {
        final String startsWithResult = startsWith(what, switches);
        return startsWithResult != null && startsWithResult.isEmpty();
    }

    // =================================================================================================================

    private void exitWithHelp(final boolean showAdvanced) {
        try {
            final String helpText = new String(
                    IOUtils.toByteArray(ArgumentsParser.class
                            .getResourceAsStream("/dsl-clc-help.txt")), "UTF-8");
            output.println(showAdvanced ? helpText : helpText.replaceFirst(
                    "(?s)# ADVANCED OPTIONS.*", ""));
        } catch (final Exception e) {
            output.println("Could not display the help file!");
        }
        System.exit(0);
    }
}
