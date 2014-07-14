package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.json.JsonObject;

import java.io.File;
import java.io.FilenameFilter;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Utils {
	public static JsonObject toJson(final Map<String, String> map) {
		final JsonObject json = new JsonObject();
		for(final Map.Entry<String, String> kv : map.entrySet()) {
			json.add(kv.getKey(), kv.getValue());
		}
		return json;
	}

	public static List<File> findDslFiles(final File path) {
		final List<File> foundFiles = new LinkedList<File>();
		findDslFiles(path, foundFiles);
		return foundFiles;
	}

	private static void findDslFiles(final File path, final List<File> foundFiles) {
		for (final File f : path.listFiles()) {
			if (f.isDirectory()) {
				findDslFiles(f, foundFiles);
			}
			else if (f.getName().endsWith(".dsl") || f.getName().endsWith(".ddd")) {
				foundFiles.add(f);
			}
		}
	}
}
