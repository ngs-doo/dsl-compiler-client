package com.dslplatform.compiler.client.api.processors;

import com.dslplatform.compiler.client.api.logging.Logger;
import com.dslplatform.compiler.client.api.transport.Message;

public class UpdateProcessor extends UpdateUnsafeProcessor {
    public UpdateProcessor(final Logger logger) {
        super(logger);
    }

    private String confirmation;

    public boolean needsConfirmation() {
        return confirmation != null;
    }

    public String getConfirmationMessage() {
        return confirmation;
    }

    @Override
    public void process(final Message message) {
        logger.trace("Received message: " + message.messageType + " " + message.info);

        switch (message.messageType) {
            case CONFIRM:
                confirmation = message.info;
                break;

            default:
                super.process(message);
                break;
        }
    }
}
