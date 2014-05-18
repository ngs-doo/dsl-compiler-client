package com.dslplatform.compiler.client.processor;

import com.dslplatform.compiler.client.api.json.JsonReader;
import com.dslplatform.compiler.client.response.Source;
import org.apache.commons.codec.Charsets;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import org.apache.commons.codec.Charsets;
import java.util.*;

public class FromJson {
    public static Map<String, String> map(final byte[] responseBody) {
        final JsonReader jsonReader =
                new JsonReader(new InputStreamReader(new ByteArrayInputStream(responseBody), Charset.forName("UTF-8")));
        try {
            return jsonReader.readStringMap();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ArrayList<String> array(final byte[] responseBody) {
        final JsonReader jsonReader =
                new JsonReader(new InputStreamReader(new ByteArrayInputStream(responseBody), Charset.forName("UTF-8")));
        try {
            return jsonReader.readStringArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<Source> orderedSources(final byte[] httpResponseBody) {
        final List<Source> sources = new LinkedList<Source>();
        for (final Map.Entry<String, String> source : map(httpResponseBody).entrySet()) {
            final String originalPath = source.getKey();
            final int langLeng = originalPath.indexOf('/');
            final String language = originalPath.substring(0, langLeng).toLowerCase();
            final int lastIndexOfDot = originalPath.lastIndexOf('.');

            final String path = originalPath.substring(langLeng, lastIndexOfDot).replace('.', '/');
            final String ext =  originalPath.substring(lastIndexOfDot);
            final StringBuilder sourceName = new StringBuilder(path).append(ext);

            sources.add(new Source(language, sourceName.toString(), source.getValue().getBytes(
                    Charsets.UTF_8))); // TODO : Better Get Bytes.
        }
        return sources;
    }
}
