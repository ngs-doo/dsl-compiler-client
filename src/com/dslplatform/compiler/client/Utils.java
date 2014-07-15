package com.dslplatform.compiler.client;

import com.dslplatform.compiler.client.json.JsonObject;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Utils {
	public static String read(final InputStream stream) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		StringBuilder sb = new StringBuilder();
		char[] buffer = new char[8192];
		int len;
		while ((len = reader.read(buffer)) != -1) {
			sb.append(buffer, 0, len);
		}
		reader.close();
		return sb.toString();
	}

	public static Either<String> readFile(final File file) {
		try {
			final String content = read(new FileInputStream(file));
			return Either.success(content);
		} catch (Exception ex) {
			return Either.fail(ex.getMessage());
		}
	}

	public static void saveFile(final File file, final String content) throws IOException {
		final Writer writer = new OutputStreamWriter(new FileOutputStream(file), "utf-8");
		writer.write(content);
		writer.close();
	}

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
		for (final String fn : path.list()) {
			final File f = new File(path, fn);
			if (f.isDirectory()) {
				findDslFiles(f, foundFiles);
			}
			else if (f.getName().endsWith(".dsl") || f.getName().endsWith(".ddd")) {
				foundFiles.add(f);
			}
		}
	}

	public static void unpackZip(File path, InputStream stream) throws IOException {
		final ZipInputStream zip = new ZipInputStream(stream);
		ZipEntry entry;
		while ((entry = zip.getNextEntry()) != null)
		{
			final File file = new File(path.getAbsolutePath() + "/" + entry.getName());
			final FileOutputStream fos = new FileOutputStream(file);
			for (int c = zip.read(); c != -1; c = zip.read()) {
				fos.write(c);
			}
			zip.closeEntry();
		}
		zip.close();
	}
}
