package com.dslplatform.compiler.client.response;

public class ParseDSLResponse extends AuthorizationResponse {
    private final boolean parsed;

    public boolean getParsed() {
        return parsed;
    }

    private final String parseMessage;

    public String getParseMessage() {
        return parseMessage;
    }

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
