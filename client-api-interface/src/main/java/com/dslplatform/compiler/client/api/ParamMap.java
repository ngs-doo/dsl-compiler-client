package com.dslplatform.compiler.client.api;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.dslplatform.compiler.client.api.params.Param;
import com.dslplatform.compiler.client.api.params.Param.XMLOut;

class ParamMap implements Comparator<Class<? extends Param>> {
    private final SortedMap<Class<? extends Param>, Set<Param>> params;

    public ParamMap() {
        params = new TreeMap<Class<? extends Param>, Set<Param>>(this);
    }

    @Override
    public int compare(
            final Class<? extends Param> p1,
            final Class<? extends Param> p2) {
        return p1.getSimpleName().compareToIgnoreCase(p2.getSimpleName());
    }

    public <T extends Param> T firstOf(final Class<T> clazz) {
        final Set<Param> paramSet = params.get(clazz);
        if (paramSet == null) {
            return null;
        }
        return clazz.cast(paramSet.iterator().next());
    }

    public void add(final Param... params) {
        for (final Param param : params) {
            if (param != null) {
                Set<Param> paramSet = this.params.get(param.getClass());
                if (paramSet == null || !param.allowMultiple()) {
                    paramSet = new LinkedHashSet<Param>();
                    this.params.put(param.getClass(), paramSet);
                }

                paramSet.add(param);
            }
        }
    }

    public String toXML() {
        final XMLOut xO = new XMLOut("payload");
        for (final Set<Param> paramSet : params.values()) {
            for (final Param param : paramSet) {
                param.addToPayload(xO);
            }
        }
        return xO.toString();
    }
}
