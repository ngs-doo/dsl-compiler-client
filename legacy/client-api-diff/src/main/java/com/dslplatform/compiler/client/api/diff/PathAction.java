package com.dslplatform.compiler.client.api.diff;

import com.dslplatform.compiler.client.api.commons.Hash;

public class PathAction implements Comparable<PathAction> {
    public final String source;
    public final String destination;
    public final Hash hash;
    public final ChangeAction action;

    public PathAction(
            final String source,
            final String destination,
            final Hash hash,
            final ChangeAction action) {
        this.source = source;
        this.hash = hash;
        this.destination = destination;
        this.action = action;
    }

    @Override
    public int compareTo(final PathAction o) {
        return source.compareToIgnoreCase(o.source);
    }

    // format: OFF
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder()
            .append(action)
            .append(": ")
            .append(source);

        if (destination != null) {
            sb.append(" -> ").append(destination);
        }

        return sb.toString();
    }
}
