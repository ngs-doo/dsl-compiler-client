package com.dslplatform.compiler.client.cmdline.processor;

import com.dslplatform.compiler.client.api.transport.Message;
import com.dslplatform.compiler.client.prompt.CLPrompt;

public class ParseHandler extends MessageHandler {
    public boolean isParsed = false;

    public ParseHandler(final CLPrompt prompt) {
        super(prompt);
    }

    public void process(final Message msg) {
        switch (msg.messageType) {
            case SUCCESS:
                accInfo.append(msg.info);
                isParsed = true;
                break;

             default:
                super.process(msg);
        }
    }
}
