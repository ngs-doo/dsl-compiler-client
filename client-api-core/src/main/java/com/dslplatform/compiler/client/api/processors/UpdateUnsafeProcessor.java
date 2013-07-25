package com.dslplatform.compiler.client.api.processors;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import com.dslplatform.compiler.client.api.transport.Message;
import com.dslplatform.compiler.client.io.Logger;

public class UpdateUnsafeProcessor extends BaseProcessor {
    public UpdateUnsafeProcessor(final Logger logger) {
        super(logger);
    }

    private final List<String> messages = new ArrayList<String>();
    private final SortedMap<String, byte[]> fileBodies = new TreeMap<String, byte[]>();

    public List<String> getMessages() {
        return messages;
    }

    public SortedMap<String, byte[]> getFileBodies() {
        return fileBodies;
    }

    @Override
    public boolean processInner(final Message message) {
        switch (message.messageType) {
            case DUMP_FILE:
                fileBodies.put(message.info, message.content);
                return true;

            case SHOW_PENDING:
                messages.add(message.info);
                return true;

            default:
                return false;
        }
    }
}
