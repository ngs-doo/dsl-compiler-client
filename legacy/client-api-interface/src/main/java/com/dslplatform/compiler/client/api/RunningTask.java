package com.dslplatform.compiler.client.api;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.UUID;

import com.dslplatform.compiler.client.api.params.Target;
import com.dslplatform.compiler.client.api.transport.Message;
import com.dslplatform.compiler.client.io.Logger;

public class RunningTask {
    private final Logger logger;
    private final ApiCall apiCall;
    private final Target target;
    private final byte[] body;
    private final int pollInterval;
    private final int timeout;

    private final UUID requestID;

    public RunningTask(
            final Logger logger,
            final ApiCall apiCall,
            final Target target,
            final byte[] body,
            final int pollInterval,
            final int timeout) throws IOException {
        this.logger = logger;
        this.apiCall = apiCall;
        this.target = target;

        this.body = body;
        this.pollInterval = pollInterval;
        this.timeout = timeout;

        requestID = UUID.randomUUID();
    }

    public Message[] getMessages() throws IOException {
        final ArrayList<Message> messageList = new ArrayList<Message>();
        for (final Message message : processMessages(null)) {
            messageList.add(message);
        }
        return messageList.toArray(new Message[messageList.size()]);
    }

    public Iterable<Message> processMessages(final MessageProcessor processor)
            throws IOException {

        final long endTime = System.currentTimeMillis() + timeout;
        boolean requestSent = false;

        while (true) {
            try {
                return processResponse(apiCall.read(target, requestID,
                        requestSent ? null : body, pollInterval), processor);

            } catch (final SocketTimeoutException e) {
                logger.trace("Timeout has occurred, retrying ...");
            } finally {
                requestSent = true;
                if (System.currentTimeMillis() > endTime) {
                    throw new IOException(
                            "A timeout has been reached for this request");
                }
            }
        }
    }

    private Iterable<Message> processResponse(
            final Response response,
            final MessageProcessor processor) throws IOException {
        final ArrayDeque<Message> messageQueue = new ArrayDeque<Message>();

        final Message[] messages = response.toMessages();

        if (messages == null) {
            throw new SocketTimeoutException();
        }

        for (final Message message : messages) {
            messageQueue.add(message);

            if (processor != null) {
                processor.process(message);
            }

            if (message.isFinal) {
                break;
            }
        }

        return messageQueue;
    }
}
