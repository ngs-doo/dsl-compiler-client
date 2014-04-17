package com.dslplatform.compiler.client.processor;

import com.dslplatform.compiler.client.api.json.JsonReader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

    public static Map<String, Map<String, String>> orderedSources(final byte[] httpResponseBody) {
        final Map<String, Map<String, String>> sources = new HashMap<String, Map<String, String>>();
        for (final Map.Entry<String, String> source : map(httpResponseBody).entrySet()) {
            final String originalPath = source.getKey();
            final int langLeng = originalPath.indexOf('/');
            final String language = originalPath.substring(0, langLeng).toLowerCase();
            final int lastIndexOfDot = originalPath.lastIndexOf('.');

            final String path = originalPath.substring(langLeng, lastIndexOfDot).replace('.', '/');
            final String ext =  originalPath.substring(lastIndexOfDot);
            final StringBuilder sourceName = new StringBuilder(path).append(ext);

            if (!sources.containsKey(language)) {
                final Map<String, String> targetSource = new HashMap<String, String>();
                sources.put(language, targetSource);
            }
            sources.get(language).put(sourceName.toString(), source.getValue());
        }
        return sources;
    }
}
