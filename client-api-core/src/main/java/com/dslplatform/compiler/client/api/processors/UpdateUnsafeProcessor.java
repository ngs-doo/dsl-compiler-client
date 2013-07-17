package com.dslplatform.compiler.client.api.processors;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import com.dslplatform.compiler.client.api.MessageProcessor;
import com.dslplatform.compiler.client.api.logging.Logger;
import com.dslplatform.compiler.client.api.transport.Message;

public class UpdateUnsafeProcessor implements MessageProcessor {
    protected final Logger logger;

    public UpdateUnsafeProcessor(final Logger logger) {
        this.logger = logger;
    }

    private boolean successful;
    private byte[] authorization;
    private String response;
    private final List<String> messages = new ArrayList<String>();
    private final SortedMap<String, byte[]> fileBodies = new TreeMap<String, byte[]>();

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

    public List<String> getMessages() {
        return messages;
    }

    public SortedMap<String, byte[]> getFileBodies() {
        return fileBodies;
    }

    @Override
    public void process(final Message message) {
        logger.trace("Received message: " + message.messageType + " " + message.info);

        switch (message.messageType) {
            case AUTH_TOKEN:
                authorization = message.content;
                break;

            case AUTH_ERROR:
                response = message.info;
                break;

            case DUMP_FILE:
                fileBodies.put(message.info, message.content);
                break;

            case SHOW_PENDING:
                messages.add(message.info);
                break;

            case SUCCESS:
                successful = true;
                response = message.info;
                break;

            case ERROR:
                response = message.info;
                break;

            default:
                logger.warn("Unsupported message received: " + message.messageType + " " + message.info);
        }
    }
}
