package com.dslplatform.compiler.client.cmdline.parser;

import com.dslplatform.compiler.client.params.CachePath;
import com.dslplatform.compiler.client.params.LoggingLevel;
import com.dslplatform.compiler.client.params.OutputPath;
import com.dslplatform.compiler.client.params.PackageName;
import com.dslplatform.compiler.client.params.ProjectID;
import com.dslplatform.compiler.client.params.ProjectName;
import com.dslplatform.compiler.client.params.Targets;
import com.dslplatform.compiler.client.params.Username;

public class CachingArgumentsProxy implements Arguments {
    private final Arguments underlying;

    public CachingArgumentsProxy(
            final Arguments underlying) {
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

}
