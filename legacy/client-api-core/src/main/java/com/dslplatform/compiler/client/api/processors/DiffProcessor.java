package com.dslplatform.compiler.client.api.processors;

import java.util.ArrayList;
import java.util.List;

import com.dslplatform.compiler.client.api.transport.Message;
import com.dslplatform.compiler.client.io.Logger;

public class DiffProcessor extends BaseProcessor {
    public DiffProcessor(
            final Logger logger) {
        super(logger);
    }

    private boolean autoConfirm;
    private final List<String> diffs = new ArrayList<String>();

    public boolean isAutoConfirm() {
        return autoConfirm;
    }

    public List<String> getDiffs() {
        return diffs;
    }

    @Override
    public boolean processInner(final Message message) {
        switch (message.messageType) {
            case DIFF:
                diffs.add(message.info);
                return true;

            case AUTO_CONFIRM:
                autoConfirm = true;
                response = message.info;
                return true;

            default:
                return false;
        }
    }
}
