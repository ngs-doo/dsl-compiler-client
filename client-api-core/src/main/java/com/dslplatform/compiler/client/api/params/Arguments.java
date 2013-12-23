package com.dslplatform.compiler.client.api.params;

import java.io.File;
import java.io.IOException;

import com.dslplatform.compiler.client.io.Logger.Level;

public interface Arguments {
    public Action getAction();

    public boolean isSkipDiff();

    public boolean isConfirmUnsafeRequired();

    public String getUsername();

    public String getPassword();

    public ProjectID getProjectID();

    public Language[] getLanguages();

    public PackageName getPackageName();

    public DSL getDsl() throws IOException;

    public File getOutputPath();

    public File getCachePath();

    public Level getLoggingLevel();

    public File getProjectIniPath();
}
