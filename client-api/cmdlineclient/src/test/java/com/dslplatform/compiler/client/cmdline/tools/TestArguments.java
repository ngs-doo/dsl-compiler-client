package com.dslplatform.compiler.client.cmdline.tools;

import com.dslplatform.compiler.client.api.config.PropertyLoader;

import com.dslplatform.compiler.client.cmdline.parser.Arguments;
import com.dslplatform.compiler.client.cmdline.parser.ArgumentsValidator;
import com.dslplatform.compiler.client.cmdline.parser.CachingArgumentsProxy;
import com.dslplatform.compiler.client.params.*;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Random;

public class TestArguments extends CachingArgumentsProxy {

    private final static String livePropsPath = "~/.config/dsl-compiler-client/dsl-clc-test.props";

    private final File tempBase = new File(System.getProperty("java.io.tmpdir"), "dcc-test");
    private final File tempFile = new File(tempBase, "" + new Random().nextInt(111111));

    private final Arguments liveArguments;

    public TestArguments(String props, Logger logger) throws IOException {
        super(logger, new ArgumentsValidator(logger, new PropertyLoader(logger).read(props)));
        Arguments underArguments = new ArgumentsValidator(logger, new PropertyLoader(logger).read(livePropsPath));
        liveArguments = new CachingArgumentsProxy(logger, underArguments);
    }

    public static Arguments make(String props, Logger logger) {
        try {
            return new TestArguments(props, logger);
        } catch (IOException e) {
            return new EmptyArguments();
        }
    }

    @Override
    public OutputPath getOutputPath() {
        return new OutputPath(new File(tempFile, cleanPath(super.getOutputPath().outputPath.getPath())));
    }

    @Override
    public MonoApplicationPath getMonoApplicationPath() {
        if (super.getMonoApplicationPath() == null ) return null;
        else return new MonoApplicationPath(new File(tempFile, cleanPath(super.getMonoApplicationPath().monoApplicationPath.getPath())));
    }

    @Override
    public CompilationTargetPath getCompilationTargetPath() {
        return new CompilationTargetPath(new File(tempFile, cleanPath(super.getCompilationTargetPath().compilationTargetPath.getPath())));
    }

    @Override
    public DSLPath getDSLPath() {
        try {
            File dslPath = new File(getClass().getResource("/test_dsl").toURI());
            return new DSLPath(dslPath);
        } catch (URISyntaxException e) {
            return new DSLPath(null);
        }
    }

    public MigrationFilePath getMigrationFilePath() {
        return new MigrationFilePath(new File(tempFile, cleanPath(super.getMigrationFilePath().migrationFilePath.getPath())));
    }

    @Override
    public Username getUsername() {
        try {
            return liveArguments.getUsername();
        } catch (IllegalArgumentException e) {
            return super.getUsername();
        }
    }

    @Override
    public ProjectID getProjectID() {
        try {
            return liveArguments.getProjectID();
        } catch (IllegalArgumentException e) {
            return super.getProjectID();
        }
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
        try {
            return liveArguments.getPassword();
        } catch (IllegalArgumentException e) {
            return super.getPassword();
        }
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

    private String cleanPath(String file) {
        if (file.startsWith("~")) {
            return file.replaceFirst("~", System.getProperty("user.home"));
        } else {
            return file;
        }
    }
}
