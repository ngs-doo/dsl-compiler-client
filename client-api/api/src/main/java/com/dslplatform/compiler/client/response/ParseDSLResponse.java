package com.dslplatform.compiler.client.response;

public class ParseDSLResponse extends AuthorizationResponse {
    public final boolean parsed;

    public final String parseMessage;

    public ParseDSLResponse(
            final boolean authorized,
            final String authorizationErrorMessage,
            final boolean parsed,
            final String parseMessage
    ) {
        super(authorized, authorizationErrorMessage);
        this.parsed = parsed;
        this.parseMessage = parseMessage;
    }
}
