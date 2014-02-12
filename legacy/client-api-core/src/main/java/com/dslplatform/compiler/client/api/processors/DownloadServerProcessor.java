package com.dslplatform.compiler.client.api.processors;

import com.dslplatform.compiler.client.api.transport.Message;
import com.dslplatform.compiler.client.io.Logger;

public class DownloadServerProcessor extends UpdateUnsafeProcessor {
    public DownloadServerProcessor(
            final Logger logger) {
        super(logger);
    }

    private byte[] serverArchive;

    public byte[] getServerArchive() {
        return serverArchive;
    }

    @Override
    public boolean processInner(final Message message) {
        switch (message.messageType) {
            case DUMP_FILE:
                serverArchive = message.content;
                return true;

            default:
                return super.processInner(message);
        }
    }
}
