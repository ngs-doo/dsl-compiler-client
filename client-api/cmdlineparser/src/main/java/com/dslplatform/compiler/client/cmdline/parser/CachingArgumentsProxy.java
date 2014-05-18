package com.dslplatform.compiler.client.cmdline.parser;

import com.dslplatform.compiler.client.params.*;

public class CachingArgumentsProxy implements Arguments {
    private final Arguments underlying;

    public CachingArgumentsProxy(
            final Arguments underlying) {
        this.underlying = underlying;
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
}
