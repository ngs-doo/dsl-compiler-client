package com.dslplatform.compiler.client.cmdline.parser;

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

    public Username getUsername();
    public ProjectID getProjectID();
    public ProjectName getProjectName();
    public PackageName getPackageName();
    public Password getPassword();
    public Targets getTargets();
    public OutputPath getOutputPath();
    public DSLPath getDSLPath();
    public CachePath getCachePath();
    public LoggingLevel getLoggingLevel();

    public boolean isWithActiveRecord();
    public boolean isWithJavaBeans();
    public boolean isWithJackson();
    public boolean isWithHelperMethods();
    public boolean isSkipDiff();
    public boolean isAllowUnsafe();
}
