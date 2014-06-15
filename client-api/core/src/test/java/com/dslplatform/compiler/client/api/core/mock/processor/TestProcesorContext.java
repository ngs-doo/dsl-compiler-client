package com.dslplatform.compiler.client.api.core.mock.processor;

import java.util.Iterator;
import java.util.List;

public abstract class TestProcesorContext {
    public String mkString(List<String> stringList) {
        StringBuilder sb = new StringBuilder();
        Iterator<String> stringIterator = stringList.iterator();
        sb.append(stringIterator.next());
        while (stringIterator.hasNext()) sb.append(", ").append(stringIterator.next());
        return sb.toString();
    }
}
