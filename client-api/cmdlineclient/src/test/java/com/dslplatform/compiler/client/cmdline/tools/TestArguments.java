package com.dslplatform.compiler.client.cmdline.tools;

import com.dslplatform.compiler.client.api.config.PropertyLoader;

import com.dslplatform.compiler.client.cmdline.parser.Arguments;
import com.dslplatform.compiler.client.cmdline.parser.ArgumentsValidator;
import com.dslplatform.compiler.client.cmdline.tools.EmptyArguments;
import com.dslplatform.compiler.client.params.*;
import org.slf4j.Logger;

import java.io.IOException;

public class TestArguments extends ArgumentsValidator {

    private final static String livePropsPath = "~/.config/dsl-compiler-client/dsl-clc-test.props";

    private final Arguments liveArguments;

    public TestArguments(String props, Logger logger) throws IOException {
        super(logger, new PropertyLoader(logger).read(props));
        Arguments tempArgs;
        try {
            tempArgs = new ArgumentsValidator(logger, new PropertyLoader(logger).read(livePropsPath));
        } catch (IOException ioe) {
            tempArgs = new EmptyArguments();
        }
        liveArguments = tempArgs;
    }

    @Override
    public Username getUsername() {
        return (liveArguments.getUsername() == null) ?  super.getUsername() : liveArguments.getUsername();
    }

    @Override
    public ProjectID getProjectID() {
        return (liveArguments.getProjectID() == null) ? super.getProjectID() : liveArguments.getProjectID();
    }

    @Override
    public ProjectName getProjectName() {
        return (liveArguments.getProjectName() == null) ? super.getProjectName() : liveArguments.getProjectName();
    }

    @Override
    public PackageName getPackageName() {
        return (liveArguments.getPackageName() == null) ? super.getPackageName() : liveArguments.getPackageName();
    }

    @Override
    public Password getPassword() {
        return (liveArguments.getPassword() == null) ? super.getPassword() : liveArguments.getPassword();
    }

    @Override
    public DBUsername getDBUsername() {
        return (liveArguments.getDBUsername() == null) ? super.getDBUsername() : liveArguments.getDBUsername();
    }

    @Override
    public DBPassword getDBPassword() {
        return (liveArguments.getDBPassword() == null) ? super.getDBPassword() : liveArguments.getDBPassword();
    }

    @Override
    public DBHost getDBHost() {
        return (liveArguments.getDBHost() == null) ? super.getDBHost() : liveArguments.getDBHost();
    }

    @Override
    public DBPort getDBPort() {
        return (liveArguments.getDBPort() == null) ? super.getDBPort() : liveArguments.getDBPort();
    }

    @Override
    public DBDatabaseName getDBDatabaseName() {
        return (liveArguments.getDBDatabaseName() == null) ? super.getDBDatabaseName() : liveArguments.getDBDatabaseName();
    }

    @Override
    public DBConnectionString getDBConnectionString() {
        return (liveArguments.getDBConnectionString() == null) ? super.getDBConnectionString() : liveArguments.getDBConnectionString();
    }
}
