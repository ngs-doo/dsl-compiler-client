package com.dslplatform.compiler.client.cmdline.params;

import com.dslplatform.compiler.client.api.MessageProcessor;
import com.dslplatform.compiler.client.api.transport.Message;
import com.dslplatform.compiler.client.cmdline.CLPrompt;

public class MessageHandler implements MessageProcessor {

    public boolean needsConfirmation = false;

    private final String invalidCredentialsRecognitionString = "is not allowed";

    protected final CLPrompt prompt;
    protected StringBuilder accInfo = new StringBuilder();

    public boolean error = false;

    public boolean callAgain = false;

    public MessageHandler(final CLPrompt prompt) {
        this.prompt = prompt;
    }

    public String getInfo() {
        return accInfo.toString();
    }

    public void ping() {
        prompt.ping();
    }

    public boolean hasError() {
        return error;
    }

    public void setError() {
        error = true;
    }

    public void print() {
        prompt.info(getInfo());
        accInfo = new StringBuilder();
    }

    public void process(final Message msg) {
        switch (msg.messageType) {
            case SHOW_PENDING:
            case SUCCESS:
                accInfo.append(msg.info + prompt.nl());
                break;

            case ERROR:
                error = true;
                if (msg.info.contains(invalidCredentialsRecognitionString))
                    callAgain = true;

            default:
                accInfo.append(msg.messageType.name() + " " + msg.info + prompt.nl());
        }
    }
}
