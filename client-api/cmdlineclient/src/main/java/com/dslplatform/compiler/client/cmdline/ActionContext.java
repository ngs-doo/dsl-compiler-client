package com.dslplatform.compiler.client.cmdline;

import com.dslplatform.compiler.client.Api;
import com.dslplatform.compiler.client.api.config.Tokenizer;
import com.dslplatform.compiler.client.cmdline.parser.Arguments;
import com.dslplatform.compiler.client.cmdline.parser.ParamSwitches;
import com.dslplatform.compiler.client.io.Output;
import com.dslplatform.compiler.client.params.DSL;
import com.dslplatform.compiler.client.params.Target;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

abstract class ActionContext {
    protected final Api api;
    protected final Logger logger;
    protected final Output output;
    protected final Arguments arguments;
    protected final CommandLinePrompt clp;
    protected final IO io;

    protected ActionContext(Api api, Logger logger, Output output, Arguments arguments, CommandLinePrompt clp, IO io) {
        this.api = api;
        this.logger = logger;
        this.output = output;
        this.arguments = arguments;
        this.clp = clp;
        this.io = io;
        init();
    }

    protected boolean skip_diff;
    protected boolean allow_unsafe;

    protected void init() {
        skip_diff = arguments.isSkipDiff();
        allow_unsafe = arguments.isAllowUnsafe();
    }

    protected String getToken() {
        return Tokenizer.basicHeader(arguments.getUsername().username, arguments.getPassword().password);
    }

    protected DSL getDSL() {
        logger.trace("Reading dsl from {}", arguments.getDSLPath().dslPath);
        try {
            DSL dsl = io.readDSL(arguments.getDSLPath().dslPath);
            skip_diff = arguments.isSkipDiff();
            return dsl;
        } catch (IOException e) {
            logger.error(e.getMessage());
            skip_diff = false;
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
            }};
        return dataSource;
    }

    protected File getRevenjPath() {
        return arguments.getRevenjPath().revenjPath;
    }

    protected File getTargetPath() {
        return new File("generatedModel.dll"); // todo - add to arguments are remove this hardcodie
    }

    /* temporary, till params penetrate the api */
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
}
