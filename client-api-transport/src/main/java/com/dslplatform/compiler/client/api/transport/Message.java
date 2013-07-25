package com.dslplatform.compiler.client.api.transport;

import java.io.Serializable;

public class Message implements Serializable {
    public final int ordinal;
    public final boolean isFinal;
    public final MessageType messageType;
    public final String info;
    public final String metadata;
    public final byte[] content;

    public Message(
            final int ordinal,
            final boolean isFinal,
            final MessageType messageType,
            final String info,
            final String metadata,
            final byte[] content) {

        this.ordinal = ordinal;
        this.isFinal = isFinal;
        this.messageType = messageType;
        this.info = info;
        this.metadata = metadata;
        this.content = content;
    }

    @Override
    public String toString() {
        return ordinal + " (" + messageType + "): " + info;
    }

    private static final long serialVersionUID = 0x00070002;
}
