package com.dslplatform.compiler.client.params;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

public class Actions implements Param {
    private final Set<Action> actionSet;

    public Actions(final Collection<Action> actions) {
        if (actions.isEmpty()) throw new IllegalArgumentException("No actionSet specified!");
        this.actionSet = EnumSet.copyOf(actions);
    }

    @Override
    public boolean equals(final Object that) {
        if (!(that instanceof Actions) || that == null) return false;

        final Actions thatActions = (Actions) that;
        return actionSet.equals(thatActions.actionSet);
    }

    @Override
    public int hashCode() {
        return actionSet.hashCode();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Actions(");
        for (final Action action : actionSet) sb.append(action).append(",");
        sb.setLength(sb.length() - 1);
        return sb.append(')').toString();
    }

    public Set<Action> getActionSet(){
        return this.actionSet;
    }
}
