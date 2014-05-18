package com.dslplatform.compiler.client.api.core.test;

import com.dslplatform.compiler.client.api.json.JsonWriter;
import org.junit.Test;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.*;


public class JsonWriterTest {

    @Test
    public void mapSerializationTest() throws IOException {
        final Map<String, Object> rootMap = new LinkedHashMap<String, Object>();
        rootMap.put("StrAttributeKey1", "StrAttributeValue1");
        rootMap.put("StrAttributeKey2", "StrAttributeValue2");

        final Map<String, Object> childMap = new LinkedHashMap<String, Object>();
        childMap.put("ChildAttributeKey1", "ChildAttributeValue1");

        rootMap.put("MapAttribute", childMap);

        JsonWriter jw = new JsonWriter();
        jw.write(rootMap);
        String serialized = jw.toString();

        assertTrue("String serialized", serialized.contains("\"StrAttributeKey2\":\"StrAttributeValue2\""));
        assertTrue("Map serialized", serialized.contains( "\"MapAttribute\":{\"ChildAttributeKey1\":\"ChildAttributeValue1\"}"));
    }

    @Test
    public void serializeByteArrayTest() throws IOException {
        final Map<String, Object> rootMap = new LinkedHashMap<String, Object>();
        rootMap.put("byteArray", "ByteArrayAttributeValue");

        JsonWriter jw = new JsonWriter();
        jw.write(rootMap);
        String serialized = jw.toString();

        assertEquals("byte [] serialized", serialized, "{\"byteArray\":\"ByteArrayAttributeValue\"}");
    }
}
