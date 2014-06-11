package com.dslplatform.compiler.client.cmdline.parser;

import org.slf4j.Logger;

import com.dslplatform.compiler.client.params.Action;
import com.dslplatform.compiler.client.params.Actions;
import com.dslplatform.compiler.client.params.CachePath;
import com.dslplatform.compiler.client.params.DBAuth;
import com.dslplatform.compiler.client.params.DBConnectionString;
import com.dslplatform.compiler.client.params.DBDatabaseName;
import com.dslplatform.compiler.client.params.DBHost;
import com.dslplatform.compiler.client.params.DBPassword;
import com.dslplatform.compiler.client.params.DBPort;
import com.dslplatform.compiler.client.params.DBUsername;
import com.dslplatform.compiler.client.params.DSLPath;
import com.dslplatform.compiler.client.params.LoggingLevel;
import com.dslplatform.compiler.client.params.OutputPath;
import com.dslplatform.compiler.client.params.PackageName;
import com.dslplatform.compiler.client.params.Password;
import com.dslplatform.compiler.client.params.ProjectID;
import com.dslplatform.compiler.client.params.ProjectName;
import com.dslplatform.compiler.client.params.ProjectPropertiesPath;
import com.dslplatform.compiler.client.params.Targets;
import com.dslplatform.compiler.client.params.Username;

public class CachingArgumentsProxy implements Arguments {
    private final Arguments underlying;

    final Logger logger;

    public CachingArgumentsProxy(final Logger logger, final Arguments underlying) {
        this.logger = logger;
        this.underlying = underlying;
    }

    private LoggingLevel loggingLevel;

    @Override
    public LoggingLevel getLoggingLevel() {
        return loggingLevel == null
                ? loggingLevel = underlying.getLoggingLevel()
                : loggingLevel;
    }

    private OutputPath outputPath;
    @Override
    public OutputPath getOutputPath() {
        return outputPath == null
                ? outputPath = underlying.getOutputPath()
                : outputPath;
    }

    private DSLPath dslPath;
    @Override
    public DSLPath getDSLPath() {
        return dslPath == null
                ? dslPath = underlying.getDSLPath()
                : dslPath;
    }

    private Password password;
    @Override
    public Password getPassword() {
        return password == null
                ? password = underlying.getPassword()
                : password;
    }

    private ProjectPropertiesPath projectPropertiesPath;
    @Override
    public ProjectPropertiesPath getProjectPropertiesPath() {
        return projectPropertiesPath == null
                ? projectPropertiesPath = underlying.getProjectPropertiesPath()
                : projectPropertiesPath;
    }

    private CachePath cachePath;

    @Override
    public CachePath getCachePath() {
        return cachePath == null
                ? cachePath = underlying.getCachePath()
                : cachePath;
    }

    private Username username;

    @Override
    public Username getUsername() {
        return username == null
                ? username = underlying.getUsername()
                : username;
    }

    private ProjectID projectID;

    @Override
    public ProjectID getProjectID() {
        return projectID == null
                ? projectID = underlying.getProjectID()
                : projectID;
    }

    private ProjectName projectName;

    @Override
    public ProjectName getProjectName() {
        return projectName == null
                ? projectName = underlying.getProjectName()
                : projectName;
    }

    private PackageName packageName;

    @Override
    public PackageName getPackageName() {
        return packageName == null
                ? packageName = underlying.getPackageName()
                : packageName;
    }

    private Targets targets;

    @Override
    public Targets getTargets() {
        return targets == null
                ? targets = underlying.getTargets()
                : targets;
    }

    private Actions actions;

    @Override
    public Actions getActions() {
        if (actions == null) {
            final Actions acts = underlying.getActions();

            validateActionsAgainstProperties(acts);

            actions = acts;
        }
        return actions;

    }

    /** Note: computed from the underlying subproperties*/
    private DBAuth dbAuth;
    @Override
    public DBAuth getDBAuth() {
        return dbAuth == null
                ? dbAuth = underlying.getDBAuth()
                : dbAuth;
    }

    private DBUsername dbUsername;
    @Override
    public DBUsername getDBUsername() {
        return dbUsername == null
                ? dbUsername = underlying.getDBUsername()
                : dbUsername;
    }

    private DBPassword dbPassword;
    @Override
    public DBPassword getDBPassword() {
        return dbPassword == null
                ? dbPassword = underlying.getDBPassword()
                : dbPassword;
    }

    private DBHost dbHost;
    @Override
    public DBHost getDBHost() {
        return dbHost == null
                ? dbHost = underlying.getDBHost()
                : dbHost;
    }

    private DBPort dbPort;
    @Override
    public DBPort getDBPort() {
        return dbPort == null
                ? dbPort = underlying.getDBPort()
                : dbPort;
    }

    private DBDatabaseName dbDatabaseName;
    @Override
    public DBDatabaseName getDBDatabaseName() {
        return dbDatabaseName == null
                ? dbDatabaseName = underlying.getDBDatabaseName()
                : dbDatabaseName;
    }

    private DBConnectionString dbConnectionString;
    @Override
    public DBConnectionString getDBConnectionString() {
        return dbConnectionString == null
                ? dbConnectionString = underlying.getDBConnectionString()
                : dbConnectionString;
    }

    private Boolean withActiveRecord;
    @Override
    public boolean isWithActiveRecord() {
        return withActiveRecord == null
                ? withActiveRecord = underlying.isWithActiveRecord()
                : withActiveRecord;
    }

    private Boolean withHelperMethods;

    @Override
    public boolean isWithHelperMethods() {
        return withHelperMethods == null
                ? withHelperMethods = underlying.isWithHelperMethods()
                : withHelperMethods;
    }

    private Boolean withJavaBeans;

    @Override
    public boolean isWithJavaBeans() {
        return withJavaBeans == null
                ? withJavaBeans = underlying.isWithHelperMethods()
                : withJavaBeans;
    }

    private Boolean withJackson;

    @Override
    public boolean isWithJackson() {
        return withJackson == null
                ? withJackson = underlying.isWithHelperMethods()
                : withJackson;
    }

    private Boolean allowUnsafe;

    @Override
    public boolean isAllowUnsafe() {
        return allowUnsafe == null
                ? allowUnsafe = underlying.isAllowUnsafe()
                : allowUnsafe;
    }

    private Boolean skipDiff;

    @Override
    public boolean isSkipDiff() {
        return skipDiff == null
                ? skipDiff = underlying.isSkipDiff()
                : skipDiff;
    }

    /**
     * Validate that {@code actionsToValidate} have all neccessary properties loaded.
     */// TODO: Clumsy for this to be here, move it maybe to another layer
    private void validateActionsAgainstProperties(final Actions actionsToValidate){
        /* To validate actions against properties, we need to check if all neccessary properties are loaded */
        logger.trace("Validating actions.");
        for(final Action action : actionsToValidate.getActionSet()){
            logger.trace("Checking if all neccessary parameters exist for the action [" + action.toString() + "] ...");
            switch(action){
                case UPDATE:
                    getProjectPropertiesPath();
                    getUsername();
                    getPassword();
                    getProjectID();
                    getPackageName();
                    getDSLPath();
                    isAllowUnsafe();
                    getTargets();
                    break;
                case CONFIG:
                    getProjectPropertiesPath();
                    getUsername();
                    getPassword();
                    getProjectID();
                    break;
                case PARSE:
                    getUsername();
                    getPassword();
                    getDSLPath();
                    break;
                case GET_CHANGES:// a.k.a. diff
                    /* (TODO: differentiate managed vs. unmanaged;
                     * or do so in a different app layer)
                     * Managed:
                     *  - project properties path (optional)
                     *  - username/password
                     *  - project ID
                     *  - DSL path
                     * Unmanaged:
                     *  - project properties path (optional)
                     *  - DSL path
                     *  - DBAuth
                     */
                    //getProjectPropertiesPath(); // Optional, no check
                    getUsername();
                    getPassword();
                    getProjectID();
                    getDSLPath();
                    getDBAuth();
                    break;
                case LAST_DSL:
                    break;
                case GENERATE_SOURCES:
                    getProjectPropertiesPath();
                    getUsername();
                    getPassword();
                    getPackageName();
                    getDSLPath();
                    getDBAuth();
                    getOutputPath();
                    break;
                case DOWNLOAD_GENERATED_MODEL:
                    getProjectPropertiesPath();
                    getUsername();
                    getPassword();
                    getProjectID();
                    getTargets();
                    break;
                case UNMANAGED_CS_SERVER:
                    // TODO:
                    break;
                case UNMANAGED_SOURCE:
                    // TODO:
                    break;
                case UNMANAGED_SQL_MIGRATION:
                    getProjectPropertiesPath();
                    getUsername();
                    getPassword();
                    getDSLPath();
                    getDBAuth();
                    getOutputPath();
                    break;
                default:
                    throw new IllegalArgumentException("Invalid action " + action);
            }
            logger.trace("Done.");
        }
    }
}
