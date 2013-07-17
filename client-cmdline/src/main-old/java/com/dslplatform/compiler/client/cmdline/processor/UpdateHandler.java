package com.dslplatform.compiler.client.cmdline.processor;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.dslplatform.compiler.client.api.transport.Message;
import com.dslplatform.compiler.client.prompt.CLPrompt;

public class UpdateHandler extends MessageHandler {

    final String outputPath;

    //private HashMap<String, byte[]> files = new HashMap<String, byte[]>();

    public UpdateHandler(final String outputPath, final CLPrompt prompt) {
        super(prompt);
        this.outputPath = outputPath;
    }

    public void process(final Message msg) {
        switch (msg.messageType) {
            case DUMP_FILE:
                try {
                    final File file = new File(this.outputPath, msg.info);
                    accInfo.append("Writing to: " + file.getCanonicalPath()).append(prompt.nl());
                    FileUtils.writeByteArrayToFile(file, msg.content);
                } catch (final IOException ioe) {
                    accInfo.append(ioe.getMessage());
                }
                break;

            case CONFIRM:
                accInfo.append(msg.info);
                needsConfirmation = true;
                break;

            default:
                super.process(msg);
        }
    }
}
