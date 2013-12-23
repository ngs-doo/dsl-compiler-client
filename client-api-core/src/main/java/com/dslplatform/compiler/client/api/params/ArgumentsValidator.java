package com.dslplatform.compiler.client.api.params;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import com.dslplatform.compiler.client.api.DSLLoader;
import com.dslplatform.compiler.client.api.commons.PathExpander;
import com.dslplatform.compiler.client.io.Logger;
import com.dslplatform.compiler.client.io.Logger.Level;

public abstract class ArgumentsValidator implements Arguments {
    /** If no action is specified, default to UPDATE */
    private static final Action DEFAULT_ACTION = Action.UPDATE;

    /** This is a Java client, so default to the Java language */
    private static final Language DEFAULT_LANGUAGE = Language.JAVA;

    /** Advanced settings must have provided defaults, ~ expands to user.home */
    private static final String DEFAULT_CACHE_PATH = "~/.dsl-platform";

    /** By default, we aren't logging anything */
    private static final Logger.Level DEFAULT_LOGGING_LEVEL = Logger.Level.ERROR;

    // =================================================================================================================

    private final Set<String> actions = new LinkedHashSet<String>();
    private boolean skipDiff = false;
    private boolean confirmUnsafeRequired = true;

    private String username = null;
    private String password = null;
    private String projectID = null;

    private final Set<String> languages = new LinkedHashSet<String>();
    private String packageName = null;
    private String projectName = null;

    private final Set<String> dslPaths = new LinkedHashSet<String>();
    private String outputPath = null;

    private String cachePath = null;
    private String loggingLevel = null;

    private String projectIniPath = null;
    private String newProjectIniPath = null;

    // =================================================================================================================

    private final Logger logger;
    private final PathExpander pathExpander;

    public ArgumentsValidator(
            final Logger logger) {
        this.logger = logger;
        pathExpander = new PathExpander(logger);
    }

    // format: OFF
    private Action consollidateActions() {
        logger.trace("About to consollidate all actions: " + actions);

        final int actNum = actions.size();

        if (actNum == 0) {
            logger.debug("No actions were specified, defaulting to: "
                    + DEFAULT_ACTION);
            return DEFAULT_ACTION;
        }

        final Set<Action> actions = new LinkedHashSet<Action>();
        validation: for (final String unparsedAction : this.actions) {
            for (final Action action : Action.values()) {
                if (unparsedAction.equalsIgnoreCase(action.name().replace('_', ' '))) {
                    actions.add(action);
                    continue validation;
                }
            }

            throw new IllegalArgumentException(String.format(
                    "Action \"%s\" does not exist!", unparsedAction));
        }

        logger.trace("Succeeded in validating all actions: " + actions);

        if (actNum == 1) {
            logger.debug("Single action was specified, returing it");
            return actions.iterator().next();
        }

        if (actNum == 2 && (
                actions.contains(Action.PARSE) && actions.contains(Action.DIFF) ||
                actions.contains(Action.PARSE) && actions.contains(Action.PARSE_AND_DIFF) ||
                actions.contains(Action.DIFF) && actions.contains(Action.PARSE_AND_DIFF))) {
            logger.debug("There were two actions, parse and diff, which were merged into a single action");
            return Action.PARSE_AND_DIFF;
        }

        if (actNum == 3 &&
                actions.contains(Action.PARSE) &&
                actions.contains(Action.DIFF) &&
                actions.contains(Action.PARSE_AND_DIFF)) {
            logger.debug("There were three actions, combinations of parse and diff, which were all merged into a single action");
            return Action.PARSE_AND_DIFF;
        }

        throw new IllegalArgumentException(
                "Incompatible actions were specified: " + actions);
    }

    @Override
    public Action getAction() {
        final Action action = consollidateActions();
        if (!confirmUnsafeRequired && action == Action.UPDATE) {
            logger.debug("Since confirmation requirement has been lifted; promoting update to update unsafe");
            return Action.UPDATE_UNSAFE;
        }
        return action;
    }

    @Override
    public boolean isSkipDiff() {
        return skipDiff;
    }

    @Override
    public boolean isConfirmUnsafeRequired() {
        return confirmUnsafeRequired;
    }

    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public ProjectID getProjectID() {
        if (projectID == null) {
            throw new NullPointerException("Project ID was not defined!");
        }

        try {
            return new ProjectID(UUID.fromString(projectID));
        } catch (final IllegalArgumentException e) {
            throw new IllegalArgumentException(String.format(
                    "\"%s\" does not look like a project ID!", projectID));
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public Language[] getLanguages() {
        final int langNum = languages.size();
        if (langNum == 0) {
            logger.debug("No languages were specified, defaulting to: "
                    + DEFAULT_LANGUAGE);
            return new Language[] { DEFAULT_LANGUAGE };
        }

        final Language[] languages = new Language[langNum];
        logger.trace("About to validate provided languages: " + this.languages);

        int index = 0;
        validation: for (final String unparsedLanguage : this.languages) {
            for (final Language language : Language.values()) {
                if (unparsedLanguage.equalsIgnoreCase(language.language)) {
                    languages[index++] = language;
                    continue validation;
                }
            }

            throw new IllegalArgumentException(String.format(
                    "Language \"%s\" is not supported.", unparsedLanguage));
        }

        logger.trace(String.format("Successfully validated %d languages",
                languages.length));
        return languages;
    }

    @Override
    public PackageName getPackageName() {
        return new PackageName(packageName);
    }

    @Override
    public ProjectName getProjectName() {
        return new ProjectName(projectName);
    }

    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public DSL getDsl() throws IOException {
        if (dslPaths.isEmpty()) {
            throw new IllegalArgumentException("No DSL paths were specified!");
        }

        logger.trace("About to pre-load DSL paths: " + dslPaths);
        final DSLLoader dslLoader = new DSLLoader(logger);
        for (final String dslPath : dslPaths) {
            logger.trace("Pre-loading DSL path: " + dslPaths);
            dslLoader.addPath(dslPath);
        }
        logger.trace("About to load DSL files via: " + dslLoader);
        return dslLoader.getDSL();
    }

    @Override
    public File getOutputPath() {
        if (outputPath != null) {
            return pathExpander.expandPath(outputPath);
        }

        throw new NullPointerException("Output path was not defined!");
    }

    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public File getCachePath() {
        final String cachePath;
        if (this.cachePath == null) {
            logger.debug("No cache path was specified, defaulting to: "
                    + DEFAULT_CACHE_PATH);
            cachePath = DEFAULT_CACHE_PATH;
        } else {
            cachePath = this.cachePath;
        }

        return pathExpander.expandPath(cachePath);
    }

    @Override
    public Logger.Level getLoggingLevel() {
        if (loggingLevel == null) {
            logger.debug("No logging level was specified, defaulting to: "
                    + DEFAULT_LOGGING_LEVEL);
            return DEFAULT_LOGGING_LEVEL;
        }

        logger.trace("About to validate provided logging level: "
                + loggingLevel);
        final Logger.Level level = Logger.Level.valueOf(loggingLevel
                .toUpperCase());

        if (level == null) {
            throw new IllegalArgumentException(String.format(
                    "Logging level \"%s\" is not supported.", loggingLevel));
        }

        logger.trace(String.format("Successfully validated logging level: "
                + level));
        return level;
    }

    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public File getProjectIniPath() {
        return projectIniPath == null ? null : pathExpander
                .expandPath(projectIniPath);
    }

    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public File getNewProjectIniPath() {
        return newProjectIniPath == null ? null : pathExpander
                .expandPath(newProjectIniPath);
    }

    // =================================================================================================================

    protected void addActions(final String actions) {
        for (final String action : actions.split(",")) {
            logger.debug("Adding action: " + action);
            this.actions.add(action);
        }
    }

    protected void setSkipDiff(final boolean skipDiff) {
        logger.debug("Setting skip diff: " + skipDiff);
        this.skipDiff = skipDiff;
    }

    protected void setConfirmUnsafeRequired(final boolean confirmUnsafeRequired) {
        logger.debug("Setting confirmUnsafeRequired: " + confirmUnsafeRequired);
        this.confirmUnsafeRequired = confirmUnsafeRequired;
    }

    // -----------------------------------------------------------------------------------------------------------------

    protected void setUsername(final String username) {
        logger.debug("Setting username: " + username);
        this.username = username;
    }

    protected void setPassword(final String password) {
        logger.debug("Setting password: " + (password == null ? null : "****"));
        this.password = password;
    }

    protected void setProjectID(final String projectID) {
        logger.debug("Setting project ID: " + projectID);
        this.projectID = projectID;
    }

    // -----------------------------------------------------------------------------------------------------------------

    protected void addLanguages(final String languages) {
        for (final String language : languages.split(",")) {
            logger.debug("Setting language: " + language);
            this.languages.add(language);
        }
    }

    protected void setPackageName(final String packageName) {
        logger.debug("Setting package name: " + packageName);
        this.packageName = packageName;
    }

    protected void setProjectName(final String projectName) {
        logger.debug("Setting project name: " + projectName);
        this.projectName = projectName;
    }

    // -----------------------------------------------------------------------------------------------------------------

    protected void addDslPath(final String dslPath) {
        logger.debug("Adding dsl path: " + dslPath);
        dslPaths.add(dslPath);
    }

    protected void setOutputPath(final String outputPath) {
        logger.debug("Setting output path: " + outputPath);
        this.outputPath = outputPath;
    }

    // -----------------------------------------------------------------------------------------------------------------

    protected void setCachePath(final String cachePath) {
        logger.debug("Setting cache path: " + cachePath);
        this.cachePath = cachePath;
    }

    protected void setLoggingLevel(final String loggingLevel) {
        logger.debug("Setting logger level: " + loggingLevel);
        this.loggingLevel = loggingLevel;
        logger.setLevel(Level.valueOf(loggingLevel.toUpperCase()));
    }

    // -----------------------------------------------------------------------------------------------------------------

    protected void setNewProjectIniPath(final String newProjectIniPath) {
        this.newProjectIniPath = newProjectIniPath;
    }

    // -----------------------------------------------------------------------------------------------------------------

    protected void setProjectIniPath(final String projectIniPath) {
        this.projectIniPath = projectIniPath;
    }

    @Override
    public void readProjectIni() throws IOException {

        if (getProjectIniPath() == null) {
            return;
        }
        final Properties properties = new Properties(); {
            final InputStream is = new FileInputStream(getProjectIniPath());
            try {
                properties.load(is);
                logger.debug("Successfully loaded project ini properties");
            } catch (final IOException e) {
                throw new IOException(
                        "An error occured whilst reading the project ini configuration",
                        e);
            } finally {
                is.close();
            }
        }

        if (Boolean.parseBoolean(properties.getProperty("skip-diff"))) {
            logger.debug("Parsed --skip-diff parameter, setting skip diff to 'true'");
            setSkipDiff(true);
        }

        if (Boolean.parseBoolean(properties.getProperty("confirm-unsafe"))) {
            logger.debug("Parsed --confirm-unsafe parameter, setting confirm unsafe required to 'false'");
            setConfirmUnsafeRequired(false);
        }

        {
            final String username = properties.getProperty("username");
            logger.debug("Parsed username parameter, overwriting old username: "
                    + username);
            if (username != null) {
                setUsername(username);
            }
        }{
            final String password = properties.getProperty("password");
            logger.debug("Parsed password parameter, overwriting old password: ****");
            if (password != null) {
                setPassword(password);
            }
        }{
            final String projectID = properties.getProperty("project-id");
            logger.debug("Parsed project ID parameter, overwriting old project ID: "
                    + projectID);
            if (projectID != null) {
                setProjectID(projectID);
            }
        }

        {
            final String languages = properties.getProperty("language");
            logger.debug("Parsed language parameter, adding languages to the list: "
                    + languages);
            if (languages != null) {
                addLanguages(languages);
            }
        }{
            final String packageName = properties.getProperty("package-name");
            logger.debug("Parsed package name parameter, overwriting old package name: "
                    + packageName);
            if (packageName != null) {
                setPackageName(packageName);
            }
        }{
            final String projectName = properties.getProperty("project-name");
            logger.debug("Parsed project name parameter, overwriting old project name: "
                    + projectName);
            if (projectName != null) {
                setProjectName(projectName);
            }
        }

        {
            final String dslPath = properties.getProperty("dsl-path");
            logger.debug("Parsed DSL path parameter, adding DSL path to the list: "
                    + dslPath);
            if (dslPath != null) {
                addDslPath(dslPath);
            }
        }{
            final String outputPath = properties.getProperty("output-path");
            logger.debug("Parsed output path parameter, overwriting old output path: "
                    + outputPath);
            if (outputPath != null) {
                setOutputPath(outputPath);
            }
        }

        {
            final String cachePath = properties.getProperty("cache-path");
            logger.debug("Parsed cache path parameter, overwriting old cache path: "
                    + cachePath);
            if (cachePath != null) {
                setCachePath(cachePath);
            }
        }
    }
}
