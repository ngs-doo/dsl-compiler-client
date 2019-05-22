package com.dslplatform.compiler.client;

import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public abstract class Utils {

	public static String read(final InputStream stream) throws IOException {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final byte[] buffer = new byte[8192];
		int len;
		while ((len = stream.read(buffer)) != -1) {
			baos.write(buffer, 0, len);
		}
		return baos.toString("UTF-8");
	}

	public static Either<String> readFile(final File file) {
		try {
			final FileInputStream stream = new FileInputStream(file);
			try {
				final String content = read(stream);
				return Either.success(content);
			} finally {
				stream.close();
			}
		} catch (IOException ex) {
			return Either.fail(ex);
		}
	}

	public static void saveFile(final Context context, final File file, final String content) throws IOException {
		context.log("Saving file: " + file.getAbsolutePath());
		final FileOutputStream fos = new FileOutputStream(file);
		try {
			final Writer writer = new OutputStreamWriter(fos, "UTF-8");
			writer.write(content);
			writer.close();
		} finally {
			fos.close();
		}
	}

	public static List<File> findFiles(final Context context, final File path, final List<String> extensions) {
		context.log("Searching for files...");
		for (final String ext : extensions) {
			context.log("Matching: " + ext);
		}
		final List<File> foundFiles = new LinkedList<File>();
		findFiles(context, path, foundFiles, extensions);
		return foundFiles;
	}

	private static void findFiles(final Context context, final File path, final List<File> foundFiles, final List<String> extensions) {
		final String[] files = path.list();
		if (files == null) return;
		for (final String fn : files) {
			final File f = new File(path, fn);
			if (f.isDirectory()) {
				findFiles(context, f, foundFiles, extensions);
			} else {
				for (final String e : extensions) {
					if (f.getName().endsWith(e)) {
						context.log("Found: " + f.getAbsolutePath());
						foundFiles.add(f);
						break;
					}
				}
			}
		}
	}

	public static long unpackZip(final Context context, final File path, final URL remoteUrl) throws IOException {
		return unpackZip(context, path, remoteUrl, new ArrayList<File>(), 3);
	}

	private static long unpackZip(
			final Context context,
			final File path,
			final URL remoteUrl,
			final ArrayList<File> unpackedFiles,
			final int retry) throws IOException {
		try {
			final URLConnection connection = remoteUrl.openConnection();
			//60 seconds timeout to prevent handing if case of site issues
			connection.setConnectTimeout(60 * 1000);
			final long lastModified = connection.getLastModified();
			final InputStream response = connection.getInputStream();
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
			return lastModified;
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
				context.warning("Retrying download... from " + remoteUrl);
				return unpackZip(context, path, remoteUrl, new ArrayList<File>(), retry - 1);
			}
			throw io;
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
			try {
				int len;
				while ((len = stream.read(buffer)) != -1) {
					fos.write(buffer, 0, len);
				}
			} finally {
				stream.close();
			}
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
		} catch (Exception ex) {
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

		private ConsumeStream(final InputStream stream, final Context context, final Charset charset) {
			this.reader = new BufferedReader(new InputStreamReader(stream, charset));
			this.context = context;
		}

		private ConsumeStream() {
			this.reader = null;
			this.context = null;
		}

		static ConsumeStream start(final InputStream stream, final Context context, final Charset charset) {
			if (stream == null) {
				return new ConsumeStream();
			}
			final ConsumeStream cs = new ConsumeStream(stream, context, charset);
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

	private static void logCommand(final Context context, final ProcessBuilder builder) {
		final StringBuilder description = new StringBuilder("Running: ");
		for (final String arg : builder.command()) {
			description.append(arg).append(" ");
		}
		context.log(description.toString());
		context.notify("EXEC", builder);
	}

	public static Either<String> findCommand(final Context context, final String path, final String name, final String contains) {
		final String simple = path != null ? new File(path, name).getAbsolutePath() : name;
		if (testCommand(context, simple, contains)) {
			context.log("Found " + name + " in " + simple);
			return Either.success(simple);
		}
		if (isWindows()) {
			final String bat = path != null ? new File(path, name + ".bat").getAbsolutePath() : name + ".bat";
			if (testCommand(context, bat, contains)) {
				context.log("Found " + name + " in " + bat);
				return Either.success(bat);
			}
			final String cmd = path != null ? new File(path, name + ".cmd").getAbsolutePath() : name + ".cmd";
			if (testCommand(context, cmd, contains)) {
				context.log("Found " + name + " in " + cmd);
				return Either.success(cmd);
			}
		}
		return Either.fail("File not found: " + name);
	}

	public static boolean testCommand(final Context context, final String command, final String contains) {
		return testCommand(context, command, contains, Collections.<String>emptyList());
	}

	public static boolean testCommand(final Context context, final String command, final String contains, final Charset charset) {
		return testCommand(context, command, contains, Collections.<String>emptyList(), charset);
	}

	public static boolean testCommand(final Context context, final String command, final String contains, final List<String> arguments) {
		return testCommand(context, command, contains, arguments, Charset.defaultCharset());
	}

	public static boolean testCommand(final Context context, final String command, final String contains, final List<String> arguments, final Charset charset) {
		try {
			final List<String> commandAndArgs = new ArrayList<String>();
			commandAndArgs.add(command);
			commandAndArgs.addAll(arguments);
			final ProcessBuilder pb = new ProcessBuilder(commandAndArgs);
			pb.environment().put("DOTNET_CLI_TELEMETRY_OPTOUT", "1");
			logCommand(context, pb);
			final Process compilation = pb.start();
			final ConsumeStream result = ConsumeStream.start(compilation.getInputStream(), null, charset);
			final ConsumeStream error = ConsumeStream.start(compilation.getErrorStream(), null, charset);
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
		return runCommand(context, command, path, arguments, Charset.defaultCharset());
	}

	public static Either<CommandResult> runCommand(final Context context, final String command, final File path, final List<String> arguments, final Charset charset) {
		try {
			final List<String> commandAndArgs = new ArrayList<String>();
			commandAndArgs.add(command);
			commandAndArgs.addAll(arguments);
			final ProcessBuilder pb = new ProcessBuilder(commandAndArgs);
			pb.environment().put("DOTNET_CLI_TELEMETRY_OPTOUT", "1");
			if (path != null) {
				pb.directory(path);
			}
			logCommand(context, pb);
			final Process compilation = pb.start();
			final ConsumeStream result = ConsumeStream.start(compilation.getInputStream(), context, charset);
			final ConsumeStream error = ConsumeStream.start(compilation.getErrorStream(), context, charset);
			final int exitCode = compilation.waitFor();
			result.join();
			error.join();
			if (result.exception != null) {
				return Either.fail(result.exception);
			}
			if (error.exception != null) {
				return Either.fail(error.exception);
			}
			return Either.success(new CommandResult(result.output.toString(), error.output.toString(), exitCode));
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
			final String[] files = path.list();
			if (files == null) return;
			for (final String fn : files) {
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
		final File[] files = path.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(extension);
			}
		});
		if (files != null && files.length > 0) {
			foundFiles.add(path);
		}
		final String[] content = path.list();
		if (content == null) return;
		for (final String fn : content) {
			final File f = new File(path, fn);
			if (f.isDirectory()) {
				findNonEmptyDirs(f, foundFiles, extension);
			}
		}
	}

	public static List<String> listSources(File source, Context context, String extension) {
		final int len = source.getAbsolutePath().length() + 1;
		final List<String> list = new ArrayList<String>();
		if (isWindows()) {
			final List<File> dirs = findNonEmptyDirs(source, extension);
			for (final File f : dirs) {
				if (f.equals(source)) {
					list.add("*" + extension);
				} else {
					list.add(f.getAbsolutePath().substring(len) + File.separator + "*" + extension);
				}
			}
		} else {
			final List<File> files = findFiles(context, source, Collections.singletonList(extension));
			for (final File f : files) {
				list.add(f.getAbsolutePath().substring(len));
			}
		}
		return list;
	}
}
