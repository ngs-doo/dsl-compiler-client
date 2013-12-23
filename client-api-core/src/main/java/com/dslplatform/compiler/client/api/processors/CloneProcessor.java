/*
package com.dslplatform.compiler.client.api.processors;

import java.util.UUID;

import com.dslplatform.compiler.client.api.transport.Message;
import com.dslplatform.compiler.client.io.Logger;

public class CloneProcessor extends BaseProcessor {

    private UUID projectID;
    private byte[] projectIni;

    public CloneProcessor(final Logger logger) {
        super(logger);
    }

    public UUID getProjectID() {
        return projectID;
    }
    public byte[] getProjectIni() {
        return projectIni;
    }

    @Override
    public boolean processInner(final Message message) {
        switch (message.messageType) {
            case PROJECT_INI:
                logger.debug("Received new project ini");
                logger.trace("New project: [" + message.info + "]" + " \ncontent:\n" + new String(message.content));
                projectIni = message.content;
                projectID = UUID.fromString(message.info);
                return true;

            default:
                return false;
        }
    }
}
*/
