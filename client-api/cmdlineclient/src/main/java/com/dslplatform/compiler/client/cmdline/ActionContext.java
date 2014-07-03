package com.dslplatform.compiler.client.cmdline;

import com.dslplatform.compiler.client.Api;
import com.dslplatform.compiler.client.api.config.Tokenizer;
import com.dslplatform.compiler.client.cmdline.parser.Arguments;
import com.dslplatform.compiler.client.cmdline.parser.ParamSwitches;
import com.dslplatform.compiler.client.diff.HashBodyMapTool;
import com.dslplatform.compiler.client.diff.PathAction;
import com.dslplatform.compiler.client.io.FileLoader;
import com.dslplatform.compiler.client.io.Hash;
import com.dslplatform.compiler.client.io.Output;
import com.dslplatform.compiler.client.params.DSL;
import com.dslplatform.compiler.client.params.Target;
import com.dslplatform.compiler.client.response.Source;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.util.*;

public abstract class ActionContext {
    protected final Api api;
    protected final Logger logger;
    protected final Output output;
    protected final Arguments arguments;
    protected final CommandLinePrompt prompt;
    protected final IO io;

    protected final boolean skip_diff;
    protected final boolean allow_unsafe;

    protected ActionContext(final Api api, final Logger logger, final Output output, final Arguments arguments, final CommandLinePrompt clp, final IO io) {
        this.api = api;
        this.logger = logger;
        this.output = output;
        this.arguments = arguments;
        this.prompt = clp;
        this.io = io;
        this.skip_diff = arguments.isSkipDiff();
        this.allow_unsafe = arguments.isAllowUnsafe();
    }

    public enum ContinueRetryQuit {Continue, Retry, Quit;}

    protected String getToken() {
        return Tokenizer.basicHeader(arguments.getUsername().username, arguments.getPassword().password);
    }

    protected DSL getDSL() {
        logger.trace("Reading the dsl from {}", arguments.getDSLPath().dslPath);
        try {
            DSL dsl = io.readDSL(arguments.getDSLPath().dslPath);
            return dsl;
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return new DSL();
    }

    protected DataSource getDataSource() {
        DataSource dataSource = new org.postgresql.ds.PGSimpleDataSource() {
            {
                setServerName(arguments.getDBHost().dbHost);
                setPortNumber(arguments.getDBPort().dbPort);
                setDatabaseName(arguments.getDBDatabaseName().dbDatabaseName);
                setUser(arguments.getDBUsername().dbUsername);
                setPassword(arguments.getDBPassword().dbPassword);
                setSsl(false);
            }
        };
        return dataSource;
    }

    protected File getRevenjPath() {
        return arguments.getRevenjPath().revenjPath;
    }

    protected File getTargetPath() {
        return arguments.getCompilationTargetPath().compilationTargetPath;
    }

    protected Set<String> mapTargets() {
        Set<String> stringSet = new HashSet<String>();
        for (Target target : arguments.getTargets().getTargetSet()) {
            stringSet.add(target.targetName);
        }
        return stringSet;
    }

    protected File getMigrationPath() {
        return arguments.getMigrationFilePath().migrationFilePath;
    }

    protected Set<String> mapOptions() {
        Set<String> stringSet = new HashSet<String>();
        if (arguments.isWithActiveRecord())
            stringSet.addAll(Arrays.asList(ParamSwitches.WITH_ACTIVE_RECORD_SWITCHES.getParamKey().paramKey));
        if (arguments.isWithHelperMethods())
            stringSet.addAll(Arrays.asList(ParamSwitches.WITH_HELPER_METHODS_SWITCHES.getParamKey().paramKey));
        if (arguments.isWithJavaBeans())
            stringSet.addAll(Arrays.asList(ParamSwitches.WITH_JAVA_BEANS_SWITCHES.getParamKey().paramKey));
        if (arguments.isWithJackson())
            stringSet.addAll(Arrays.asList(ParamSwitches.WITH_JACKSON_SWITCHES.getParamKey().paramKey));
        return stringSet;
    }

    /**
     * This method will first look into received sources to see what languages are present in the list
     * only the languages which are present in will be managed.
     */
    protected void updateFiles(
            final Logger logger,
            final List<Source> fileBodies,
            final File outputPath) throws IOException {

        if (!outputPath.exists()) {
            io.mkdirs(outputPath);
        }

        final Set<String> languagePaths = new LinkedHashSet<String>();
        for (final Source source : fileBodies) {
            languagePaths.add(source.language);
        }

        final FileLoader fileLoader = new FileLoader();
        for (final String path : languagePaths) {
            final File languagePath = new File(outputPath.getPath(), path);

            if (languagePath.isDirectory()) {
                logger.trace("Marking managed directory: {}", languagePath);
                fileLoader.addPath(outputPath.getPath(), languagePath.getPath());
            }
        }

        final Map<Hash, SortedSet<String>> oldHash = fileLoader
                .getHashBodyMap();

        final FileLoader newFilesLoader = new FileLoader();

        for (final Source source : fileBodies) {
            String rawRealPath = cleanFilename(source.language + "/" + source.path);
            newFilesLoader.addBytes(rawRealPath, source.content);
        }

        final Map<Hash, SortedSet<String>> newHash = newFilesLoader.getHashBodyMap();
        final Map<String, PathAction> actions = HashBodyMapTool.compareHashBodyMaps(oldHash, newHash);
        final SortedMap<String, byte[]> newFiles = newFilesLoader.getBodies();

        for (final Map.Entry<String, PathAction> hashAction : actions.entrySet()) {
            final String hash = hashAction.getKey();
            final PathAction action = hashAction.getValue();

            final File source = action.source == null ? null : new File(outputPath, action.source);

            final File destination = action.destination == null
                    ? null
                    : new File(outputPath, action.destination);

            logger.debug("Performing action: {} on file {} {}", action.action.name(), source, (destination == null ? "" : " -> dest:" + destination));

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
                        } else throw e;
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
