package com.dslplatform.compiler.client;

import com.dslplatform.compiler.client.json.JsonObject;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Utils {
	public static String read(final InputStream stream) throws IOException {
		final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		final StringBuilder sb = new StringBuilder();
		final char[] buffer = new char[8192];
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
		final Writer writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
		writer.write(content);
		writer.close();
	}

	public static JsonObject toJson(final Map<String, String> map) {
		final JsonObject json = new JsonObject();
		for (final Map.Entry<String, String> kv : map.entrySet()) {
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
			} else if (f.getName().endsWith(".dsl") || f.getName().endsWith(".ddd")) {
				foundFiles.add(f);
			}
		}
	}

	public static void unpackZip(final File path, final InputStream stream) throws IOException {
		final ZipInputStream zip = new ZipInputStream(new BufferedInputStream(stream));
		ZipEntry entry;
		final byte[] buffer = new byte[8192];
		while ((entry = zip.getNextEntry()) != null) {
			final File file = new File(path.getAbsolutePath() + "/" + entry.getName());
			final FileOutputStream fos = new FileOutputStream(file);
			int len;
			while ((len = zip.read(buffer)) != -1) {
				fos.write(buffer, 0, len);
			}
			zip.closeEntry();
		}
		zip.close();
	}

	public static void downloadFile(final File file, final URL url) throws IOException {
		final byte[] buffer = new byte[8192];
		final FileOutputStream fos = new FileOutputStream(file);
		final InputStream stream = new BufferedInputStream(url.openConnection().getInputStream());
		int len;
		while ((len = stream.read(buffer)) != -1) {
			fos.write(buffer, 0, len);
		}
		stream.close();
		fos.close();
	}

	public static synchronized Either<Document> readXml(final InputStream stream) {
		try {
			final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			return Either.success(dBuilder.parse(stream));
		} catch (Exception ex) {
			return Either.fail(ex.getMessage());
		}
	}

	public static boolean testCommand(final String command, final String contains) {
		try {
			final Process compilation = Runtime.getRuntime().exec(command);
			final String result = compilation.getInputStream() != null ? Utils.read(compilation.getInputStream()) : "";
			final String error = compilation.getErrorStream() != null ? Utils.read(compilation.getErrorStream()) : "";
			compilation.waitFor();
			return error.contains(contains) || result.contains(contains);
		} catch (Exception ex) {
			return false;
		}
	}

	public static boolean isWindows() {
		return System.getProperty("os.name").contains("Windows");
	}

	public static class CommandResult {
		public final String output;
		public final String error;

		public CommandResult(final String output, final String error) {
			this.output = output;
			this.error = error;
		}
	}

	private static class ConsumeStream extends Thread {
		private final BufferedReader reader;
		private final StringBuilder output = new StringBuilder();
		private Exception exception;

		public ConsumeStream(final InputStream stream) {
			if (stream != null) {
				this.reader = new BufferedReader(new InputStreamReader(stream));
			} else {
				this.reader = null;
			}
		}

		@Override
		public void run() {
			if (reader == null) {
				return;
			}
			final char[] buffer = new char[8192];
			int len;
			try {
				while ((len = reader.read(buffer)) != -1) {
					output.append(buffer, 0, len);
				}
				reader.close();
			} catch (Exception ex) {
				exception = ex;
			}
		}
	}

	public static Either<CommandResult> runCommand(final String command, final File path) {
		try {
			final Process compilation = Runtime.getRuntime().exec(command, null, path);
			final ConsumeStream result = new ConsumeStream(compilation.getInputStream());
			final ConsumeStream error = new ConsumeStream(compilation.getErrorStream());
			result.start();
			error.start();
			compilation.waitFor();
			if (result.exception != null) {
				return Either.fail(result.exception.getMessage());
			}
			if (error.exception != null) {
				return Either.fail(error.exception.getMessage());
			}
			return Either.success(new CommandResult(result.output.toString(), error.output.toString()));
		} catch (Exception ex) {
			return Either.fail(ex.getMessage());
		}
	}
}
