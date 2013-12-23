package com.dslplatform.compiler.client.api.processors;

import com.dslplatform.compiler.client.api.transport.Message;
import com.dslplatform.compiler.client.io.Logger;

public class UpdateProcessor extends UpdateUnsafeProcessor {
    public UpdateProcessor(
            final Logger logger) {
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
    public boolean processInner(final Message message) {
        switch (message.messageType) {
            case CONFIRM:
                confirmation = message.info;
                return true;

            default:
                return super.processInner(message);
        }
    }
}
