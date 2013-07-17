package com.dslplatform.compiler.client.api.params;

import java.io.IOException;

import com.dslplatform.compiler.client.api.logging.Logger.Level;

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
    public String getOutputPath();
    public String getCachePath();
    public Level getLoggingLevel();
}
