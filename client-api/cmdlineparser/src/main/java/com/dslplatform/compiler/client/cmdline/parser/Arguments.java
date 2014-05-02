package com.dslplatform.compiler.client.cmdline.parser;

import com.dslplatform.compiler.client.params.CachePath;
import com.dslplatform.compiler.client.params.LoggingLevel;
import com.dslplatform.compiler.client.params.OutputPath;
import com.dslplatform.compiler.client.params.PackageName;
import com.dslplatform.compiler.client.params.ProjectID;
import com.dslplatform.compiler.client.params.ProjectName;
import com.dslplatform.compiler.client.params.Targets;
import com.dslplatform.compiler.client.params.Username;

public interface Arguments {

    public LoggingLevel getLoggingLevel();

    public OutputPath getOutputPath();
    public CachePath getCachePath();

    public Username getUsername();

    public ProjectID getProjectID();

    public ProjectName getProjectName();

    public PackageName getPackageName();

    public Targets getTargets();

    public boolean isWithActiveRecord();
    public boolean isWithJavaBeans();
    public boolean isWithJackson();
    public boolean isWithHelperMethods();
    public boolean isAllowUnsafe();
    public boolean isSkipDiff();
}
