package com.dslplatform.compiler.client.response;

public class CompileCSharpServerResponse {
    public final boolean compilationSuccessful;
    public final String compilationMessage;

    public CompileCSharpServerResponse(boolean compilationSuccessful, String compilationMessage) {
        this.compilationSuccessful = compilationSuccessful;
        this.compilationMessage = compilationMessage;
    }
}
