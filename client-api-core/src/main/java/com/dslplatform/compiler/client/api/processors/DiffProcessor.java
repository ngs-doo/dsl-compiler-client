package com.dslplatform.compiler.client.api.processors;

import java.util.ArrayList;
import java.util.List;

import com.dslplatform.compiler.client.api.MessageProcessor;
import com.dslplatform.compiler.client.api.logging.Logger;
import com.dslplatform.compiler.client.api.transport.Message;

public class DiffProcessor implements MessageProcessor {
    private final Logger logger;

    public DiffProcessor(final Logger logger) {
        this.logger = logger;
    }

    private boolean autoConfirm;
    private byte[] authorization;
    private String response;
    private final List<String> diffs = new ArrayList<String>();

    public boolean isAutoConfirm() {
        return autoConfirm;
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

    public List<String> getDiffs() {
        return diffs;
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

            case DIFF:
                diffs.add(message.info);
                break;

            case ERROR:
                response = message.info;
                break;

            case AUTO_CONFIRM:
                autoConfirm = true;
                response = message.info;
                break;

            case SUCCESS:
                response = message.info;
                break;

            default:
                logger.warn("Unsupported message received: " + message.messageType + " " + message.info);
        }
    }
}
