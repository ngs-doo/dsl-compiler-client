package com.dslplatform.compiler.client;

import com.dslplatform.compiler.client.json.JsonObject;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URL;
import java.util.*;
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
		} catch (IOException ex) {
			return Either.fail(ex);
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

	public static void unpackZip(final Context context, final File path, final InputStream stream) throws IOException {
		final ZipInputStream zip = new ZipInputStream(new BufferedInputStream(stream));
		ZipEntry entry;
		final byte[] buffer = new byte[8192];
		long size;
		while ((entry = zip.getNextEntry()) != null) {
			size = 0;
			final File file = new File(path.getAbsolutePath() + "/" + entry.getName());
			final FileOutputStream fos = new FileOutputStream(file);
			int len;
			while ((len = zip.read(buffer)) != -1) {
				fos.write(buffer, 0, len);
				size += len;
			}
			context.log("Unpacked: " + entry.getName() + ". Size: " + (size / 1024) + "kB");
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
		} catch (IOException ex) {
			return Either.fail(ex);
		} catch (ParserConfigurationException ex) {
			return Either.fail(ex);
		} catch (SAXException ex) {
			return Either.fail(ex);
		}
	}

	public static boolean isWindows() {
		return System.getProperty("os.name").toLowerCase().contains("windows");
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
		private final Context context;
		private final StringBuilder output = new StringBuilder();
		private IOException exception;

		private ConsumeStream(final InputStream stream, final Context context) {
			this.reader = new BufferedReader(new InputStreamReader(stream));
			this.context = context;
		}

		private ConsumeStream() {
			this.reader = null;
			this.context = null;
		}

		public static ConsumeStream start(final InputStream stream, final Context context) {
			if (stream == null) {
				return new ConsumeStream();
			}
			final ConsumeStream cs = new ConsumeStream(stream, context);
			cs.start();
			return cs;
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
					if (context != null) {
						context.log(buffer, len);
					}
				}
				reader.close();
			} catch (IOException ex) {
				exception = ex;
			}
		}
	}

	public static boolean testCommand(final Context context, final String command, final String contains, final String... arguments) {
		try {
			final List<String> commandAndArgs = new ArrayList<String>();
			commandAndArgs.add(command);
			Collections.addAll(commandAndArgs, arguments);
			final ProcessBuilder pb = new ProcessBuilder(commandAndArgs);
			final Process compilation = pb.start();
			final ConsumeStream result = ConsumeStream.start(compilation.getInputStream(), null);
			final ConsumeStream error = ConsumeStream.start(compilation.getErrorStream(), null);
			compilation.waitFor();
			return error.output.toString().contains(contains) || result.output.toString().contains(contains);
		} catch (IOException ex) {
			context.log(ex.getMessage());
			return false;
		} catch (InterruptedException ex) {
			context.log(ex.getMessage());
			return false;
		}
	}

	public static Either<CommandResult> runCommand(final Context context, final String command, final File path, final List<String> arguments) {
		try {
			final List<String> commandAndArgs = new ArrayList<String>();
			commandAndArgs.add(command);
			commandAndArgs.addAll(arguments);
			final ProcessBuilder pb = new ProcessBuilder(commandAndArgs);
			if (path != null) {
				pb.directory(path);
			}
			final Process compilation = pb.start();
			final ConsumeStream result = ConsumeStream.start(compilation.getInputStream(), context);
			final ConsumeStream error = ConsumeStream.start(compilation.getErrorStream(), context);
			compilation.waitFor();
			if (result.exception != null) {
				return Either.fail(result.exception);
			}
			if (error.exception != null) {
				return Either.fail(error.exception);
			}
			return Either.success(new CommandResult(result.output.toString(), error.output.toString()));
		} catch (IOException ex) {
			return Either.fail(ex);
		} catch (InterruptedException ex) {
			return Either.fail(ex);
		}
	}

	public static void deletePath(final File path) throws IOException {
		for (final String fn : path.list()) {
			final File f = new File(path, fn);
			if (f.isDirectory()) {
				deletePath(f);
			}
			if (!f.delete()) {
				throw new IOException("Error cleaning up temporary resource. Failed to delete: " + f.getAbsolutePath());
			}
		}
	}

	public static List<File> findNonEmptyDirs(final File path, final String extension) {
		final List<File> foundDirs = new LinkedList<File>();
		findNonEmptyDirs(path, foundDirs, extension);
		return foundDirs;
	}

	private static void findNonEmptyDirs(final File path, final List<File> foundFiles, final String extension) {
		if (path.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(extension);
			}
		}).length > 0) {
			foundFiles.add(path);
		}
		for (final String fn : path.list()) {
			final File f = new File(path, fn);
			if (f.isDirectory()) {
				findNonEmptyDirs(f, foundFiles, extension);
			}
		}
	}
}
