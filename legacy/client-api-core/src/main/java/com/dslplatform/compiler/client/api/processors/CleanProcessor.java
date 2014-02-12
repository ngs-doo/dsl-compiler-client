package com.dslplatform.compiler.client.api.processors;

import com.dslplatform.compiler.client.api.transport.Message;
import com.dslplatform.compiler.client.io.Logger;

public class CleanProcessor extends BaseProcessor {
    public CleanProcessor(
            final Logger logger) {
        super(logger);
    }

    @Override
    public boolean processInner(final Message message) {
        return false;
    }
}
