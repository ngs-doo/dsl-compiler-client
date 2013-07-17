package com.dslplatform.compiler.client.api.processors;

import com.dslplatform.compiler.client.api.MessageProcessor;
import com.dslplatform.compiler.client.api.logging.Logger;
import com.dslplatform.compiler.client.api.transport.Message;

public class ParseProcessor implements MessageProcessor {
    private final Logger logger;

    public ParseProcessor(final Logger logger) {
        this.logger = logger;
    }

    private boolean successful;
    private byte[] authorization;
    private String response;

    public boolean isSuccessful() {
        return successful;
    }

    public boolean isAuthorized() {
        return authorization != null;
    }

    public byte[] getAuthorization() {
        return authorization;
    }

    public String getResponse() {
        return response;
    }

    @Override
    public void process(final Message message) {
        logger.trace("Received message: " + message.messageType + " " + message.info);

        switch (message.messageType) {
            case AUTH_ERROR:
                response = message.info;
                break;

            case AUTH_TOKEN:
                authorization = message.content;
                break;

            case ERROR:
                response = message.info;
                break;

            case SUCCESS:
                successful = true;
                response = message.info;
                break;

            default:
                logger.warn("Unsupported message received: " + message.messageType + " " + message.info);
        }
    }
}
