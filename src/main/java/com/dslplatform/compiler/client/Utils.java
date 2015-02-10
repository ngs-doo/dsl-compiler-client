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
			FileInputStream stream = new FileInputStream(file);
			final String content = read(stream);
			stream.close();
			return Either.success(content);
		} catch (IOException ex) {
			return Either.fail(ex);
		}
	}

	public static void saveFile(final File file, final String content) throws IOException {
		FileOutputStream fos = new FileOutputStream(file);
		final Writer writer = new OutputStreamWriter(fos, "UTF-8");
		writer.write(content);
		writer.close();
		fos.close();
	}

	public static JsonObject toJson(final Map<String, String> map) {
		final JsonObject json = new JsonObject();
		for (final Map.Entry<String, String> kv : map.entrySet()) {
			json.add(kv.getKey(), kv.getValue());
		}
		return json;
	}

	public static List<File> findFiles(final File path, final List<String> extensions) {
		final List<File> foundFiles = new LinkedList<File>();
		findFiles(path, foundFiles, extensions);
		return foundFiles;
	}

	private static void findFiles(final File path, final List<File> foundFiles, final List<String> extensions) {
		for (final String fn : path.list()) {
			final File f = new File(path, fn);
			if (f.isDirectory()) {
				findFiles(f, foundFiles, extensions);
			} else {
				for (final String e : extensions) {
					if (f.getName().endsWith(e)) {
						foundFiles.add(f);
						break;
					}
				}
			}
		}
	}

	public static void unpackZip(final Context context, final File path, final URL remoteUrl) throws IOException {
		unpackZip(context, path, remoteUrl, new ArrayList<File>(), 3);
	}

	private static void unpackZip(
			final Context context,
			final File path,
			final URL remoteUrl,
			final ArrayList<File> unpackedFiles,
			final int retry) throws IOException {
		try {
			InputStream response = remoteUrl.openConnection().getInputStream();
			final ZipInputStream zip = new ZipInputStream(new BufferedInputStream(response));
			ZipEntry entry;
			final byte[] buffer = new byte[8192];
			while ((entry = zip.getNextEntry()) != null) {
				long size = 0;
				final File file = new File(path, entry.getName());
				unpackedFiles.add(file);
				final FileOutputStream fos = new FileOutputStream(file);
				int len;
				while ((len = zip.read(buffer)) != -1) {
					fos.write(buffer, 0, len);
					size += len;
				}
				fos.close();
				context.log("Unpacked: " + entry.getName() + ". Size: " + (size / 1024) + "kB");
				zip.closeEntry();
			}
			zip.close();
		} catch (IOException io) {
			context.error(io);
			for (final File f : unpackedFiles) {
				if (f.delete()) {
					context.log("Cleaned up: " + f);
				} else {
					context.log("Failed to clean up: " + f);
				}
			}
			if (retry > 0) {
				context.log("Retrying download... from " + remoteUrl);
				unpackZip(context, path, remoteUrl, new ArrayList<File>(), retry - 1);
			} else throw io;
		}
	}

	public static void downloadFile(final File file, final URL url) throws IOException {
		downloadFileAndRetry(file, url, 3);
	}

	private static void downloadFileAndRetry(final File file, final URL url, final int retry) throws IOException {
		final FileOutputStream fos = new FileOutputStream(file);
		try {
			final byte[] buffer = new byte[8192];
			final InputStream stream = new BufferedInputStream(url.openConnection().getInputStream());
			int len;
			while ((len = stream.read(buffer)) != -1) {
				fos.write(buffer, 0, len);
			}
			stream.close();
		} catch (IOException io) {
			if (retry > 0) {
				downloadFileAndRetry(file, url, retry - 1);
			} else throw io;
		} finally {
			fos.close();
		}
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
		public final int exitCode;

		public CommandResult(final String output, final String error, final int exitCode) {
			this.output = output;
			this.error = error;
			this.exitCode = exitCode;
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

	public static boolean testCommand(final Context context, final String command, final String contains) {
		return testCommand(context, command, contains, new ArrayList<String>());
	}

	public static boolean testCommand(final Context context, final String command, final String contains, final List<String> arguments) {
		try {
			final List<String> commandAndArgs = new ArrayList<String>();
			commandAndArgs.add(command);
			commandAndArgs.addAll(arguments);
			final ProcessBuilder pb = new ProcessBuilder(commandAndArgs);
			final Process compilation = pb.start();
			final ConsumeStream result = ConsumeStream.start(compilation.getInputStream(), null);
			final ConsumeStream error = ConsumeStream.start(compilation.getErrorStream(), null);
			compilation.waitFor();
			result.join();
			error.join();
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
			result.join();
			error.join();
			if (result.exception != null) {
				return Either.fail(result.exception);
			}
			if (error.exception != null) {
				return Either.fail(error.exception);
			}
			return Either.success(new CommandResult(result.output.toString(), error.output.toString(), compilation.exitValue()));
		} catch (IOException ex) {
			return Either.fail(ex);
		} catch (InterruptedException ex) {
			return Either.fail(ex);
		}
	}

	public static void deletePath(final File path) throws IOException {
		deletePathAndRetry(path, 3);
	}

	private static void deletePathAndRetry(final File path, final int retry) throws IOException {
		try {
			for (final String fn : path.list()) {
				final File f = new File(path, fn);
				if (f.isDirectory()) {
					deletePath(f);
				}
				if (!f.delete()) {
					throw new IOException("Error cleaning up temporary resource. Failed to delete: " + f.getAbsolutePath());
				}
			}
		} catch (IOException io) {
			if (retry > 0) {
				deletePathAndRetry(path, retry - 1);
			} else throw io;
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
