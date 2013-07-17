package com.dslplatform.compiler.client.cmdline.processor;

import com.dslplatform.compiler.client.api.MessageProcessor;
import com.dslplatform.compiler.client.api.transport.Message;
import com.dslplatform.compiler.client.prompt.CLPrompt;
import static com.dslplatform.compiler.client.api.params.Action.*;

public class ParseAndDiffHandler extends MessageHandler implements MessageProcessor {
    public boolean isNewProject = false;
    public boolean isParsed = false;

    private String noConformationRecognitionStringAbsence = "No change in file:";
    public ParseAndDiffHandler(final CLPrompt prompt) {
        super(prompt);
    }

    public void process(final Message msg) {
        switch (msg.messageType) {
            case DIFF:
                if (! msg.info.contains(noConformationRecognitionStringAbsence))
                    needsConfirmation = true;
                accInfo.append(msg.info + prompt.nl());
                break;

            case CONFIRM:
                accInfo.append(msg.info + prompt.nl());
                isParsed = true;
                break;

            case SUCCESS:
                accInfo.append(msg.info + prompt.nl());
                isParsed = true;
                break;

            case NEW_PROJECT:
                isNewProject = true;
                accInfo.append(msg.info + prompt.nl());
                break;

             default: super.process(msg);
        }
    }
}
