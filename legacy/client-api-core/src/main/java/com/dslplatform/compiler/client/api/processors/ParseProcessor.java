package com.dslplatform.compiler.client.api.processors;

import com.dslplatform.compiler.client.api.transport.Message;
import com.dslplatform.compiler.client.io.Logger;

public class ParseProcessor extends BaseProcessor {
    public ParseProcessor(
            final Logger logger) {
        super(logger);
    }

    @Override
    public boolean processInner(final Message message) {
        return false;
    }
}
