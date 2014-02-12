package com.dslplatform.compiler.client.api.processors;

import com.dslplatform.compiler.client.api.transport.Message;
import com.dslplatform.compiler.client.io.Logger;

public class DownloadServerModelProcessor extends UpdateUnsafeProcessor {
    public DownloadServerModelProcessor(
            final Logger logger) {
        super(logger);
    }

    private byte[] serverModel;

    public byte[] getServerModel() {
        return serverModel;
    }

    @Override
    public boolean processInner(final Message message) {
        switch (message.messageType) {
            case DUMP_FILE:
                serverModel = message.content;
                return true;

            default:
                return super.processInner(message);
        }
    }
}
