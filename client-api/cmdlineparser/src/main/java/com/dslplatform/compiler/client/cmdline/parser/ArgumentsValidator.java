package com.dslplatform.compiler.client.cmdline.parser;

import static com.dslplatform.compiler.client.cmdline.parser.ParamDefaults.ALLOW_UNSAFE_DEFAULT;
import static com.dslplatform.compiler.client.cmdline.parser.ParamDefaults.PACKAGE_NAME_DEFAULT;
import static com.dslplatform.compiler.client.cmdline.parser.ParamDefaults.SKIP_DIFF_DEFAULT;
import static com.dslplatform.compiler.client.cmdline.parser.ParamDefaults.TARGET_DEFAULT;
import static com.dslplatform.compiler.client.cmdline.parser.ParamDefaults.WITH_ACTIVE_RECORD_DEFAULT;
import static com.dslplatform.compiler.client.cmdline.parser.ParamDefaults.WITH_HELPER_METHODS_DEFAULT;
import static com.dslplatform.compiler.client.cmdline.parser.ParamDefaults.WITH_JACKSON_DEFAULT;
import static com.dslplatform.compiler.client.cmdline.parser.ParamDefaults.WITH_JAVA_BEANS_DEFAULT;
import static com.dslplatform.compiler.client.cmdline.parser.ParamKey.ACTIONS_KEY;
import static com.dslplatform.compiler.client.cmdline.parser.ParamKey.ALLOW_UNSAFE_KEY;
import static com.dslplatform.compiler.client.cmdline.parser.ParamKey.CACHE_PATH_KEY;
import static com.dslplatform.compiler.client.cmdline.parser.ParamKey.DB_CONNECTION_STRING_KEY;
import static com.dslplatform.compiler.client.cmdline.parser.ParamKey.DB_DATABASE_NAME_KEY;
import static com.dslplatform.compiler.client.cmdline.parser.ParamKey.DB_HOST_KEY;
import static com.dslplatform.compiler.client.cmdline.parser.ParamKey.DB_PASSWORD_KEY;
import static com.dslplatform.compiler.client.cmdline.parser.ParamKey.DB_PORT_KEY;
import static com.dslplatform.compiler.client.cmdline.parser.ParamKey.DB_USERNAME_KEY;
import static com.dslplatform.compiler.client.cmdline.parser.ParamKey.LOGGING_LEVEL_KEY;
import static com.dslplatform.compiler.client.cmdline.parser.ParamKey.OUTPUT_PATH_KEY;
import static com.dslplatform.compiler.client.cmdline.parser.ParamKey.PACKAGE_NAME_KEY;
import static com.dslplatform.compiler.client.cmdline.parser.ParamKey.PASSWORD_KEY;
import static com.dslplatform.compiler.client.cmdline.parser.ParamKey.PROJECT_ID_KEY;
import static com.dslplatform.compiler.client.cmdline.parser.ParamKey.PROJECT_NAME_KEY;
import static com.dslplatform.compiler.client.cmdline.parser.ParamKey.PROJECT_PROPERTIES_PATH_KEY;
import static com.dslplatform.compiler.client.cmdline.parser.ParamKey.SKIP_DIFF_KEY;
import static com.dslplatform.compiler.client.cmdline.parser.ParamKey.TARGET_KEY;
import static com.dslplatform.compiler.client.cmdline.parser.ParamKey.USERNAME_KEY;
import static com.dslplatform.compiler.client.cmdline.parser.ParamKey.WITH_ACTIVE_RECORD_KEY;
import static com.dslplatform.compiler.client.cmdline.parser.ParamKey.WITH_HELPER_METHODS_KEY;
import static com.dslplatform.compiler.client.cmdline.parser.ParamKey.WITH_JACKSON_KEY;
import static com.dslplatform.compiler.client.cmdline.parser.ParamKey.WITH_JAVA_BEANS_KEY;

import java.io.File;
import java.util.EnumSet;
import java.util.Properties;
import java.util.UUID;

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
import com.dslplatform.compiler.client.params.Target;
import com.dslplatform.compiler.client.params.Targets;
import com.dslplatform.compiler.client.params.Username;

public class ArgumentsValidator implements Arguments {
    private final Logger logger;
    private final Properties properties;

    public ArgumentsValidator(
            final Logger logger,
            final Properties properties) {
        this.logger = logger;
        this.properties = properties;
    }

    @Override
    public LoggingLevel getLoggingLevel() {
    final String loggingLevel_strName = properties.getProperty(LOGGING_LEVEL_KEY.paramKey);
        logger.trace("Validating LoggingLevel [{}] ...", loggingLevel_strName);
        if (loggingLevel_strName == null) throw new IllegalArgumentException("Logging level was not defined!");
        if (!LoggingLevel.contains(loggingLevel_strName) ) throw new IllegalArgumentException("The given logging level is undefined: "+loggingLevel_strName);

        final LoggingLevel result = LoggingLevel.valueOf(loggingLevel_strName);
        logger.debug("Retrieved OutputPath from the properties [{}]", result);
        return result;
    }

    @Override
    public OutputPath getOutputPath() {
    final String outputPath = properties.getProperty(OUTPUT_PATH_KEY.paramKey);
        logger.trace("Validating OutputPath [{}] ...", outputPath);
        if (outputPath == null) throw new IllegalArgumentException("Output path was not defined!");
        final OutputPath result = new OutputPath(new File(outputPath));
        logger.debug("Retrieved OutputPath from the properties [{}]", result);
        return result;
    }

    @Override
    public DSLPath getDSLPath() {
    final String dslPath = properties.getProperty(OUTPUT_PATH_KEY.paramKey);
        logger.trace("Validating DSLPath [{}] ...", dslPath);
        if (dslPath == null) throw new IllegalArgumentException("Output path was not defined!");
        final DSLPath result = new DSLPath(new File(dslPath));
        logger.debug("Retrieved DSLPath from the properties [{}]", result);
        return result;
    }

    @Override
    public ProjectPropertiesPath getProjectPropertiesPath() {
    final String projectPropertiesPath = properties.getProperty(PROJECT_PROPERTIES_PATH_KEY.paramKey);
        logger.trace("Validating ProjectPropertiesPath [{}] ...", projectPropertiesPath);
        if (projectPropertiesPath == null) throw new IllegalArgumentException("ProjectProperties path was not defined!");
        final ProjectPropertiesPath result = new ProjectPropertiesPath(new File(projectPropertiesPath));
        logger.debug("Retrieved ProjectPropertiesPath from the properties [{}]", result);
        return result;
    }

    @Override
    public CachePath getCachePath() {
    final String cachePath = properties.getProperty(CACHE_PATH_KEY.paramKey);
        logger.trace("Validating CachePath [{}] ...", cachePath);
        if (cachePath == null) throw new IllegalArgumentException("Cache path was not defined!");
        final CachePath result = new CachePath(new File(cachePath));
        logger.debug("Retrieved CachePath from the properties [{}]", result);
        return result;
    }

    @Override
    public Username getUsername() {
        final String username = properties.getProperty(USERNAME_KEY.paramKey);
        logger.trace("Validating Username [{}] ...", username);
        if (username == null) throw new IllegalArgumentException("Username was not defined!");
        final Username result = new Username(username);
        logger.debug("Retrieved Username from the properties [{}]", result);
        return result;
    }

    @Override
    public Password getPassword() {
        final String password = properties.getProperty(PASSWORD_KEY.paramKey);
        logger.trace("Validating Password [{}] ...", password);
        if (password == null) throw new IllegalArgumentException("Password was not defined!");
        final Password result = new Password(password);
        logger.debug("Retrieved Password from the properties [{}]", result);
        return result;
    }

    /** Note: computed from the underlying subproperties*/
    @Override
    public DBAuth getDBAuth() {

        final DBUsername dbUsername = getDBUsername();
        final DBPassword dbPassword = getDBPassword();
        final DBHost dbHost = getDBHost();
        final DBPort dbPort = getDBPort();
        final DBDatabaseName dbDatabaseName = getDBDatabaseName();
        final DBConnectionString dbConnectionString = getDBConnectionString();

        final String username = dbUsername.dbUsername;
        final String password = dbPassword.dbPassword;
        final String host = dbHost.dbHost;
        final Integer port = dbPort.dbPort;
        final String databaseName = dbDatabaseName.dbDatabaseName;
        final String connectionString = dbConnectionString.dbConnectionString;

        if((connectionString == null)
            &&
            (username == null
             || password == null
             || host == null
             || port == null
             || databaseName == null
             || connectionString == null)){
                throw
                new IllegalArgumentException("Illegal database authentication parameters. The authentication requires either a valid connection string, or all individual connection parameters set.");
         }

        return new DBAuth(
                getDBUsername()
                , getDBPassword()
                , getDBHost()
                , getDBPort()
                , getDBDatabaseName()
                , getDBConnectionString());
    }

    @Override
    public DBUsername getDBUsername() {
        final String dbusername = properties.getProperty(DB_USERNAME_KEY.paramKey);
        logger.trace("Validating DBUsername [{}] ...", dbusername);
//        if (dbusername == null) throw new IllegalArgumentException("DBUsername was not defined!");
        final DBUsername result = new DBUsername(dbusername);
        logger.debug("Retrieved DBUsername from the properties [{}]", result);
        return result;
    }

    @Override
    public DBPassword getDBPassword() {
        final String dbpassword = properties.getProperty(DB_PASSWORD_KEY.paramKey);
        logger.trace("Validating DBPassword [{}] ...", dbpassword);
//        if (dbpassword == null) throw new IllegalArgumentException("DBPassword was not defined!");
        final DBPassword result = new DBPassword(dbpassword);
        logger.debug("Retrieved DBPassword from the properties [{}]", result);
        return result;
    }

    @Override
    public DBHost getDBHost() {
        final String DBhost = properties.getProperty(DB_HOST_KEY.paramKey);
        logger.trace("Validating DBHost [{}] ...", DBhost);
//        if (DBhost == null) throw new IllegalArgumentException("DBHost was not defined!");
        final DBHost result = new DBHost(DBhost);
        logger.debug("Retrieved DBHost from the properties [{}]", result);
        return result;
    }

    @Override
    public DBPort getDBPort() {
        final String DBport = properties.getProperty(DB_PORT_KEY.paramKey);
        logger.trace("Validating DBPort [{}] ...", DBport);
//        if (DBport == null) throw new IllegalArgumentException("DBPort was not defined!");
        final DBPort result = new DBPort(DBport);
        logger.debug("Retrieved DBPort from the properties [{}]", result);
        return result;
    }

    @Override
    public DBDatabaseName getDBDatabaseName() {
        final String DBdatabasename = properties.getProperty(DB_DATABASE_NAME_KEY.paramKey);
        logger.trace("Validating DBDatabaseName [{}] ...", DBdatabasename);
//        if (DBdatabasename == null) throw new IllegalArgumentException("DBDatabaseName was not defined!");
        final DBDatabaseName result = new DBDatabaseName(DBdatabasename);
        logger.debug("Retrieved DBDatabaseName from the properties [{}]", result);
        return result;
    }

    @Override
    public DBConnectionString getDBConnectionString() {
        final String DBconnectionstring = properties.getProperty(DB_CONNECTION_STRING_KEY.paramKey);
        logger.trace("Validating DBConnectionString [{}] ...", DBconnectionstring);
//        if (DBconnectionstring == null) throw new IllegalArgumentException("DBConnectionString was not defined!");
        final DBConnectionString result = new DBConnectionString(DBconnectionstring);
        logger.debug("Retrieved DBConnectionString from the properties [{}]", result);
        return result;
    }

    @Override
    public ProjectID getProjectID() {
        final String projectID = properties.getProperty(PROJECT_ID_KEY.paramKey);
        logger.trace("Validating ProjectID [{}] ...", projectID);
        if (projectID == null) throw new IllegalArgumentException("ProjectID was not defined!");
        final ProjectID result;
        try {
            result = new ProjectID(UUID.fromString(projectID));
        } catch (final IllegalArgumentException e) {
            throw new IllegalArgumentException(String.format(
                    "\"%s\" does not look like a ProjectID!", projectID));
        }
        logger.debug("Retrieved ProjectID from the properties [{}]", result);
        return result;
    }

    @Override
    public ProjectName getProjectName() {
        final String projectName = properties.getProperty(PROJECT_NAME_KEY.paramKey);
        logger.trace("Validating ProjectName [{}] ...", projectName);
        if (projectName == null) throw new IllegalArgumentException("ProjectName was not defined!");
        final ProjectName result = new ProjectName(projectName);
        logger.debug("Retrieved ProjectName from the properties [{}]", result);
        return result;
    }

    @Override
    public PackageName getPackageName() {
        String packageName = properties.getProperty(PACKAGE_NAME_KEY.paramKey);
        logger.trace("Validating PackageName [{}] ...", packageName);
        if (packageName == null) {
            packageName = PACKAGE_NAME_DEFAULT.defaultValue;
            logger.trace("PackageName was not defined, defaulting to [{}]", packageName);
        }
        final PackageName result = new PackageName(packageName);
        logger.debug("Retrieved PackageName from the properties [{}]", result);
        return result;
    }

    @Override
    public Targets getTargets() {
        String target = properties.getProperty(TARGET_KEY.paramKey);
        logger.trace("Validating Target(s) [{}] ...", target);
        if (target == null) {
            target = TARGET_DEFAULT.defaultValue;
            logger.trace("Target(s) were not defined, defaulting to [{}]", target);
        }

        final EnumSet<Target> targetSet = EnumSet.noneOf(Target.class);
        for (final String currentTarget : target.split("\\s*,+\\s*")) {
            logger.trace("Validating Target [{}] ...", currentTarget);
            final Target foundTarget = Target.find(currentTarget);
            if (foundTarget == null) throw new IllegalArgumentException(
                    "Target [" + currentTarget + "] does not exist, valid targets are: " + Target.getValidTargets());

            targetSet.add(foundTarget);
        }

        final Targets result = new Targets(targetSet);
        logger.debug("Retrieved Target(s) from the properties [{}]", result);
        return result;
    }

    @Override
    public Actions getActions() {
        final String actions = properties.getProperty(ACTIONS_KEY.paramKey);
        logger.trace("Validating Action(s) [{}] ...", actions);

        final EnumSet<Action> actionSet = EnumSet.noneOf(Action.class);
        for (final String action : actions.split("\\s*,+\\s*")) {
            logger.trace("Validating Action [{}] ...", action);
            final Action foundAction = Action.find(action);
            if (foundAction == null) throw new IllegalArgumentException(
                    "Action [" + action + "] does not exist, valid actions are: " + Action.getValidActions());

            actionSet.add(foundAction);
        }

        final Actions result = new Actions(actionSet);
        logger.debug("Retrieved Action(s) from the properties [{}]", result);
        return result;
    }

    @Override
    public boolean isWithActiveRecord() {
        String withActiveRecord = properties.getProperty(WITH_ACTIVE_RECORD_KEY.paramKey);
        if (withActiveRecord == null) {
            withActiveRecord = WITH_ACTIVE_RECORD_DEFAULT.defaultValue;
            logger.trace("WithActiveRecord was not defined, defaulting to [{}]", withActiveRecord);
        }
        final boolean result = booleanValue(withActiveRecord);
        logger.debug("Retrieved WithActiveRecord from properties [{}]", result);
        return result;
    }

    @Override
    public boolean isWithJavaBeans() {
        String withJavaBeans = properties.getProperty(WITH_JAVA_BEANS_KEY.paramKey);
        if (withJavaBeans == null) {
            withJavaBeans = WITH_JAVA_BEANS_DEFAULT.defaultValue;
            logger.trace("WithJavaBeans was not defined, defaulting to [{}]", withJavaBeans);
        }
        final boolean result = booleanValue(withJavaBeans);
        logger.debug("Retrieved WithJavaBeans from properties [{}]", result);
        return result;
    }

    @Override
    public boolean isWithHelperMethods() {
        String withHelperMethods = properties.getProperty(WITH_HELPER_METHODS_KEY.paramKey);
        if (withHelperMethods == null) {
            withHelperMethods = WITH_HELPER_METHODS_DEFAULT.defaultValue;
            logger.trace("WithHelperMethods was not defined, defaulting to [{}]", withHelperMethods);
        }
        final boolean result = booleanValue(withHelperMethods);
        logger.debug("Retrieved WithHelperMethods from properties [{}]", result);
        return result;
    }

    @Override
    public boolean isWithJackson() {
        String withJackson = properties.getProperty(WITH_JACKSON_KEY.paramKey);
        if (withJackson == null) {
            withJackson = WITH_JACKSON_DEFAULT.defaultValue;
            logger.trace("WithJackson was not defined, defaulting to [{}]", withJackson);
        }
        final boolean result = booleanValue(withJackson);
        logger.debug("Retrieved WithJackson from properties [{}]", result);
        return result;
    }

    @Override
    public boolean isAllowUnsafe() {
        String allowUnsafe = properties.getProperty(ALLOW_UNSAFE_KEY.paramKey);
        if (allowUnsafe == null) {
            allowUnsafe = ALLOW_UNSAFE_DEFAULT.defaultValue;
            logger.trace("AllowUnsafe was not defined, defaulting to [{}]", allowUnsafe);
        }
        final boolean result = booleanValue(allowUnsafe);
        logger.debug("Retrieved AllowUnsafe from properties [{}]", result);
        return result;
    }

    @Override
    public boolean isSkipDiff() {
        String skipDiff = properties.getProperty(SKIP_DIFF_KEY.paramKey);
        if (skipDiff == null) {
            skipDiff = SKIP_DIFF_DEFAULT.defaultValue;
            logger.trace("SkipDiff was not defined, defaulting to [{}]", skipDiff);
        }
        final boolean result = booleanValue(skipDiff);
        logger.debug("Retrieved SkipDiff from properties [{}]", result);
        return result;
    }

    private static boolean booleanValue(final String value) {
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes") || value.equals("1")) return true;
        if (value.equalsIgnoreCase("false") || value.equalsIgnoreCase("no") || value.equals("0")) return false;

        throw new IllegalArgumentException("Illegal boolean value [" + value + "], allowed values are true/1/yes and false/0/no");
    }
}
