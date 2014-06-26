package com.dslplatform.compiler.client.cmdline;

import com.dslplatform.compiler.client.Api;
import com.dslplatform.compiler.client.IO;
import com.dslplatform.compiler.client.api.config.Tokenizer;
import com.dslplatform.compiler.client.cmdline.parser.Arguments;
import com.dslplatform.compiler.client.cmdline.parser.ParamSwitches;
import com.dslplatform.compiler.client.io.Output;
import com.dslplatform.compiler.client.params.Action;
import com.dslplatform.compiler.client.params.DBConnectionString;
import com.dslplatform.compiler.client.params.DSL;
import com.dslplatform.compiler.client.params.Target;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.util.*;

public abstract class ActionContext implements CLCAction {
    protected final Api api;
    protected final Logger logger;
    protected final Output output;
    protected final Arguments arguments;
    protected final CommandLinePrompt prompt;

    protected final boolean skip_diff;
    protected final boolean allow_unsafe;

    protected ActionContext(final Api api, final Logger logger, final Output output, final Arguments arguments, final CommandLinePrompt clp) {
        this.api = api;
        this.logger = logger;
        this.output = output;
        this.arguments = arguments;
        this.prompt = clp;
        this.skip_diff = arguments.isSkipDiff();
        this.allow_unsafe = arguments.isAllowUnsafe();
    }

    public void process() {
        for (Action action : arguments.getActions().getActionSet()) {
            switch (action) {
                case UPDATE:
                    upgrade();
                    break;
                case GET_CHANGES:
                    getChanges();
                    break;
                case LAST_DSL:
                    lastDSL();
                    break;
                case CONFIG:
                /* todo - managed action */
                    break;
                case PARSE:
                    parseDSL();
                    break;
                case GENERATE_SOURCES:
                    generateSources();
                    break;
                case UNMANAGED_CS_SERVER:
                    deployUnmanagedServer();
                    break;
                case UNMANAGED_SOURCE:
                    unmanagedSource();
                    break;
                case UPGRADE_UNMANAGED_DATABASE:
                    upgradeUnmanagedDatabase();
                    break;
                case UNMANAGED_SQL_MIGRATION:
                    upgradeUnmanagedDatabase();
                    break;
                case DEPLOY_UNMANAGED_SERVER:
                    deployUnmanagedServer();
                    break;
            }
        }
    }
    
    public enum ContinueRetryQuit {Continue, Retry, Quit;}

    protected String getToken() {
        return Tokenizer.basicHeader(arguments.getUsername().username, arguments.getPassword().password);
    }

    protected DSL getDSL() {
        logger.trace("Reading the dsl from {}", arguments.getDSLPath().dslPath);
        try {
            DSL dsl = IO.readDSL(arguments.getDSLPath().dslPath);
            return dsl;
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return new DSL();
    }

    protected DataSource getDataSource() {
        DBConnectionString dbConnectionString = arguments.getDBConnectionString();
        final DataSource dataSource;
        if (dbConnectionString.dbConnectionString == null) {
            dataSource = new org.postgresql.ds.PGSimpleDataSource() {
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
        } else {
            throw new RuntimeException("Connection strings are not supported yet!");
        }
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

    protected DBConnectionString getConnectionString() {
        final String connectionStringFormat = "server=%s;port=%d;database=%s;user=%s;password=%s;encoding=unicode";
        final String connectionString = String.format(connectionStringFormat, arguments.getDBHost().dbHost, arguments.getDBPort().dbPort, arguments.getDBDatabaseName().dbDatabaseName, arguments.getDBUsername().dbUsername, arguments.getPassword().password);
        return new DBConnectionString(connectionString);
    }
}
