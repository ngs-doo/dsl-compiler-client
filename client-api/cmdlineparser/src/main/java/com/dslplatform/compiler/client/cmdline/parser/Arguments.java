package com.dslplatform.compiler.client.cmdline.parser;

import com.dslplatform.compiler.client.params.*;

public interface Arguments {

    public ProjectPropertiesPath getProjectPropertiesPath();

    public Actions getActions();

    public DBAuth getDBAuth();

    public DBUsername getDBUsername();
    public DBPassword getDBPassword();
    public DBHost getDBHost();
    public DBPort getDBPort();
    public DBDatabaseName getDBDatabaseName();
    public DBConnectionString getDBConnectionString();

    public MigrationFilePath getMigrationFilePath();

    public Username getUsername();
    public ProjectID getProjectID();
    public ProjectName getProjectName();
    public PackageName getPackageName();
    public Password getPassword();
    public Targets getTargets();
    public OutputPath getOutputPath();
    public DSLPath getDSLPath();
    public CachePath getCachePath();
    public RevenjPath getRevenjPath();
    public LoggingLevel getLoggingLevel();

    public RevenjVersion getRevenjVersion();
    public MonoApplicationPath getMonoApplicationPath();
    public CompilationTargetPath getCompilationTargetPath();

    public boolean isWithActiveRecord();
    public boolean isWithJavaBeans();
    public boolean isWithJackson();
    public boolean isWithHelperMethods();
    public boolean isSkipDiff();
    public boolean isAllowUnsafe();
    public boolean isManaged();
}
