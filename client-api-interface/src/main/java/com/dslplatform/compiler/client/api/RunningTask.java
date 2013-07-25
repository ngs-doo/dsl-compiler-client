package com.dslplatform.compiler.client.api;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.UUID;

import com.dslplatform.compiler.client.api.params.Target;
import com.dslplatform.compiler.client.api.transport.Message;

public class RunningTask {
    public final Target target;
    public final UUID requestID;
    public final int pollInterval;
    public final int timeout;

    public RunningTask(
            final Target target,
            final Response response,
            final int pollInterval,
            final int timeout) throws IOException {

        this.target = target;
        this.pollInterval = pollInterval;
        this.timeout = timeout;

        final String body = response.bodyToString();
        try {
            if (!response.isOK()) {
                throw new IOException("Invalid API response: " + body);
            }

            requestID = UUID.fromString(body);
        } catch (final IllegalArgumentException e) {
            throw new IOException("Invalid API response: " + body);
        }
    }

    public Message[] getMessages() throws IOException {
        final ArrayList<Message> messageList = new ArrayList<Message>();
        for (final Message message : processMessages(null)) {
            messageList.add(message);
        }
        return messageList.toArray(new Message[messageList.size()]);
    }

    public Iterable<Message> processMessages(final MessageProcessor processor) throws IOException {
        final ArrayDeque<Message> messageQueue = new ArrayDeque<Message>();

        long endTime = System.currentTimeMillis() + timeout;
        int lastOrdinal = -1;

        while (true) {
            final byte[] body = new byte[0];
            final Response response = ApiCall.await(target, requestID, pollInterval, lastOrdinal, body);

            for (final Message message : response.getMessages()) {
                endTime = System.currentTimeMillis() + timeout;
                messageQueue.add(message);

                if (processor != null) processor.process(message);
                if (message.isFinal) {
                    return messageQueue;
                }
            }

            if (System.currentTimeMillis() > endTime) {
                throw new IOException("A timeout has been reached for this request");
            }
        }
    }
}
