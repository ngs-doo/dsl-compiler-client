package com.dslplatform.compiler.client.api.processors;

import com.dslplatform.compiler.client.api.MessageProcessor;
import com.dslplatform.compiler.client.api.transport.Message;
import com.dslplatform.compiler.client.io.Logger;

abstract class BaseProcessor implements MessageProcessor {
    protected final Logger logger;

    public BaseProcessor(
            final Logger logger) {
        this.logger = logger;
    }

    protected byte[] authorization;
    protected boolean successful;
    protected String response;

    public boolean isAuthorized() {
        return authorization != null;
    }

    public byte[] getAuthorization() {
        return authorization;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public String getResponse() {
        return response;
    }

    @Override
    public void process(final Message message) {
        logger.debug("Received message: " + message.messageType + " "
                + message.info);

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
                if (!processInner(message)) {
                    logger.error("Unsupported message received: "
                            + message.messageType + " " + message.info);
                }
        }
    }

    protected abstract boolean processInner(final Message message);
}
