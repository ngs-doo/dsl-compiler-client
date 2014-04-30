package com.dslplatform.compiler.client.params;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

public class Targets implements Param {
    private final Set<Target> targets;

    public Targets(final Collection<Target> targets) {
        if (targets.isEmpty()) throw new IllegalArgumentException("No targets specified!");
        this.targets = EnumSet.copyOf(targets);
    }

    @Override
    public boolean equals(final Object that) {
        if (!(that instanceof Targets) || that == null) return false;

        final Targets thatTargets = (Targets) that;
        return targets.equals(thatTargets.targets);
    }

    @Override
    public int hashCode() {
        return targets.hashCode();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Targets(");
        for (final Target target : targets) sb.append(target).append(",");
        sb.setLength(sb.length() - 1);
        return sb.append(')').toString();
    }
}
