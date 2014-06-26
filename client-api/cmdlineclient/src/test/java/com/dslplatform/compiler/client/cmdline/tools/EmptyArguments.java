package com.dslplatform.compiler.client.cmdline.tools;

import com.dslplatform.compiler.client.cmdline.parser.Arguments;
import com.dslplatform.compiler.client.params.*;

public class EmptyArguments implements Arguments {
    @Override
    public ProjectPropertiesPath getProjectPropertiesPath() {
        return null;
    }

    @Override
    public boolean isManaged() {
        return false;
    }

    @Override
    public MonoApplicationPath getMonoApplicationPath() {
        return null;
    }

    @Override
    public CompilationTargetPath getCompilationTargetPath() {
        return null;
    }

    @Override
    public Actions getActions() {
        return null;
    }

    @Override
    public DBAuth getDBAuth() {
        return null;
    }

    @Override
    public DBUsername getDBUsername() {
        return null;
    }

    @Override
    public DBPassword getDBPassword() {
        return null;
    }

    @Override
    public DBHost getDBHost() {
        return null;
    }

    @Override
    public DBPort getDBPort() {
        return null;
    }

    @Override
    public DBDatabaseName getDBDatabaseName() {
        return null;
    }

    @Override
    public DBConnectionString getDBConnectionString() {
        return null;
    }

    @Override
    public MigrationFilePath getMigrationFilePath() {
        return null;
    }

    @Override
    public Username getUsername() {
        return null;
    }

    @Override
    public ProjectID getProjectID() {
        return null;
    }

    @Override
    public ProjectName getProjectName() {
        return null;
    }

    @Override
    public PackageName getPackageName() {
        return null;
    }

    @Override
    public Password getPassword() {
        return null;
    }

    @Override
    public Targets getTargets() {
        return null;
    }

    @Override
    public OutputPath getOutputPath() {
        return null;
    }

    @Override
    public DSLPath getDSLPath() {
        return null;
    }

    @Override
    public CachePath getCachePath() {
        return null;
    }

    @Override
    public RevenjPath getRevenjPath() {
        return null;
    }

    @Override
    public LoggingLevel getLoggingLevel() {
        return null;
    }

    @Override
    public boolean isWithActiveRecord() {
        return false;
    }

    @Override
    public boolean isWithJavaBeans() {
        return false;
    }

    @Override
    public boolean isWithJackson() {
        return false;
    }

    @Override
    public boolean isWithHelperMethods() {
        return false;
    }

    @Override
    public boolean isSkipDiff() {
        return false;
    }

    @Override
    public boolean isAllowUnsafe() {
        return false;
    }
}
