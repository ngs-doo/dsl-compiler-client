package com.dslplatform.compiler.client.api;

import com.dslplatform.compiler.client.api.transport.Message;

public interface MessageProcessor {
    public void process(final Message message);
}
