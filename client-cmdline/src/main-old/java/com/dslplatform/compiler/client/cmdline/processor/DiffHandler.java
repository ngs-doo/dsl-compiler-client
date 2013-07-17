package com.dslplatform.compiler.client.cmdline.processor;

import com.dslplatform.compiler.client.api.MessageProcessor;
import com.dslplatform.compiler.client.api.transport.Message;
import com.dslplatform.compiler.client.prompt.CLPrompt;

public class DiffHandler extends MessageHandler implements MessageProcessor {

    public boolean isNew = false;

    public DiffHandler(final CLPrompt prompt) {
        super(prompt);
    }

    public void process(final Message msg) {
        switch (msg.messageType) {
            case NEW_PROJECT:
                isNew = true;
                accInfo.append(msg.info);
                break;

            case DIFF:
                accInfo.append(msg.info);
                break;

            default:
                super.process(msg);
        }
    }
}
