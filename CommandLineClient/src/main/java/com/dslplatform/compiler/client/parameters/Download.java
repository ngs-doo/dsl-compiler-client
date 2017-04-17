package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;

public enum Download implements CompileParameter {
	INSTANCE;

	@Override
	public String getAlias() {
		return "download";
	}

	@Override
	public String getUsage() {
		return null;
	}

	private static final String DEFAULT_REMOTE_URL = "https://tools.dsl-platform.com/";

	public static String remoteUrl(final Context context) {
		final String customURL = context.get(Download.INSTANCE);
		if (customURL != null && !customURL.isEmpty()) {
			return customURL;
		}
		return DEFAULT_REMOTE_URL;
	}

	private static String websiteName(final Context context) {
		final String remoteUrl = remoteUrl(context);
		if (DEFAULT_REMOTE_URL.equals(remoteUrl)) {
			return "DSL Platform";
		}
		return "custom url (" + remoteUrl + ")";
	}

	public static long downloadAndUnpack(final Context context, final String file, final File path) throws IOException {
		final URL server = new URL(Download.remoteUrl(context) + file + ".zip");
		final String websiteName = websiteName(context);
		context.show("Downloading " + file + ".zip from " + websiteName + "...");
		return Utils.unpackZip(context, path, server);
	}

	public static Either<Long> lastModified(final Context context, final String file, final String name, final long current) {
		final String websiteName = websiteName(context);
		try {
			final URL server = new URL(Download.remoteUrl(context) + file + ".zip");
			context.log("Checking last modified info for " + file + ".zip from " + websiteName + "...");
			final HttpURLConnection connection = (HttpURLConnection) server.openConnection();
			connection.setRequestMethod("HEAD");
			//60 seconds timeout to prevent handing if case of site issues
			connection.setConnectTimeout(30 * 1000);
			connection.setReadTimeout(30 * 1000);
			final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			final long latest = connection.getLastModified();
			if (latest == 0 && connection.getResponseCode() == -1) {
				return Either.fail("Unable to check " + file + " version on " + websiteName + ". Internet connection not available?");
			}
			if (current == latest && current != 0) {
				context.show(name + " at latest version (" + sdf.format(latest) + ")");
			} else if (current > 0 && latest > 0) {
				context.show("Different version of " + name + " found at " + websiteName + ".");
				context.show("Local version: " + sdf.format(current));
				context.show("Upstream version: " + sdf.format(latest));
			} else {
				if (latest == 0) {
					context.warning(name + " not found at " + websiteName + " website.");
				} else {
					context.show("Upstream version: " + sdf.format(latest));
				}
			}
			return Either.success(latest);
		} catch (UnknownHostException ex) {
			return Either.fail("Unable to check for " + file + " on " + websiteName + ".", ex);
		} catch (IOException ex) {
			return Either.fail(ex);
		}
	}

	public static boolean downloadZip(
			final File dependencies,
			final Context context,
			final String name,
			final String zip) {
		final String websiteName = websiteName(context);
		try {
			context.show("Downloading " + name + " from " + websiteName + "...");
			final long lastModified = downloadAndUnpack(context, zip, dependencies);
			if (!dependencies.setLastModified(lastModified)) {
				context.warning("Unable to set last modified info on: " + dependencies.getAbsolutePath());
			}
		} catch (IOException ex) {
			context.error("Error downloading dependencies from " + websiteName + ".");
			context.error(ex);
			return false;
		}
		return true;
	}

	private static boolean promptForAlternative(
			final File dependencies,
			final Context context,
			final String name,
			final String zip) throws ExitException {
		final String answer;
		if (!context.contains(INSTANCE)) {
			if (!context.canInteract()) {
				throw new ExitException();
			}
			final String websiteName = websiteName(context);
			answer = context.ask("Try alternative download from " + websiteName + " (y/N):");
		} else {
			answer = "y";
		}
		if (!"y".equalsIgnoreCase(answer)) {
			throw new ExitException();
		}
		return downloadZip(dependencies, context, name, zip);
	}

	public static boolean checkJars(
			final Context context,
			final String name,
			final String zip,
			final String id,
			final String path,
			final String... libraries) throws ExitException {
		final File dependencies = Dependencies.getDependencies(context, name, id, zip, true);
		final File[] found = dependencies.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".jar");
			}
		});
		if (found == null || found.length == 0) {
			if (zip == null && libraries.length == 0) {
				context.log("No dependencies defined for: " + name);
				return true;
			}
			context.warning(name + " not found in: " + dependencies.getAbsolutePath());
			if (!context.contains(INSTANCE)) {
				if (!context.canInteract()) {
					context.error("Download option not enabled. Enable download option, change dependencies path or place " + name + " files in specified folder.");
					throw new ExitException();
				}
				final String answer = context.ask("Do you wish to download latest " + name + " version from the Internet (y/N):");
				if (!"y".equalsIgnoreCase(answer)) {
					throw new ExitException();
				}
			}
			final Either<String> tryMaven = libraries.length > 0 && path != null ? Maven.findMaven(context) : Either.<String>fail("Library not defined");
			if (!tryMaven.isSuccess()) {
				if (zip == null) {
					context.error("Unable to find Maven. Dependency can't be downloaded.");
					throw new ExitException();
				}
				return downloadZip(dependencies, context, name, zip);
			}
			for (final String library : libraries) {
				if (!downloadLibrary(context, name, path, dependencies, tryMaven, library, zip)) {
					return false;
				}
			}
			final Either<Long> lastModified = lastModified(context, zip, name, 0);
			if (lastModified.isSuccess()) {
				if (!dependencies.setLastModified(lastModified.get())) {
					context.warning("Unable to set last modified info on: " + dependencies.getAbsolutePath());
				}
			} else {
				context.warning("Failed to check dependency version for: " + dependencies.getAbsolutePath());
			}
		}
		return true;
	}

	private static boolean downloadLibrary(
			final Context context,
			final String name,
			final String path,
			final File dependencies,
			final Either<String> tryMaven,
			final String library,
			final String zip) throws ExitException {
		context.show("Downloading " + name + " (" + library + ") from Sonatype...");
		try {
			final URL maven = new URL("https://oss.sonatype.org/content/repositories/releases/" + path + "/" + library + "/maven-metadata.xml");
			final Either<Document> doc = Utils.readXml(maven.openConnection().getInputStream());
			if (!doc.isSuccess()) {
				context.error("Error downloading library info from Sonatype.");
				context.error(doc.whyNot());
				return false;
			}
			final Element root = doc.get().getDocumentElement();
			final Element versioning = (Element) root.getElementsByTagName("versioning").item(0);
			final String version = versioning.getElementsByTagName("release").item(0).getTextContent();
			final String sharedUrl = "https://oss.sonatype.org/content/repositories/releases/" +
					path + "/" + library + "/" + version + "/" + library + "-" + version;
			final URL pomUrl = new URL(sharedUrl + ".pom");
			final File pomFile = new File(dependencies, library + "-" + version + ".pom");
			Utils.downloadFile(pomFile, pomUrl);
			final URL jarUrl = new URL(sharedUrl + ".jar");
			Utils.downloadFile(new File(dependencies, library + "-" + version + ".jar"), jarUrl);
			context.show("Downloading " + name + " library dependencies with Maven...");
			final Either<Utils.CommandResult> gatherDeps =
					Utils.runCommand(
							context,
							tryMaven.get(),
							pomFile.getParentFile(),
							Arrays.asList(
									"dependency:copy-dependencies",
									"\"-DoutputDirectory=" + dependencies.getAbsolutePath() + "\"",
									"\"-f=" + pomFile.getAbsolutePath() + "\""));
			if (!gatherDeps.isSuccess()) {
				context.error("Error gathering dependencies with Maven.");
				context.error(gatherDeps.whyNot());
				return promptForAlternative(dependencies, context, name, zip);
			}
			final String result = gatherDeps.get().output + gatherDeps.get().error;
			if (!result.contains("BUILD SUCCESS")) {
				context.error("Maven error during dependency download.");
				context.show(result);
				return promptForAlternative(dependencies, context, name, zip);
			}
		} catch (IOException ex) {
			context.error("Unable to download " + name + " from Sonatype.");
			context.error(ex);
			return promptForAlternative(dependencies, context, name, zip);
		}
		return true;
	}

	@Override
	public boolean check(final Context context) {
		final String value = context.get(Download.INSTANCE);
		if (value != null && value.length() > 0) {
			if (!value.endsWith("/")) {
				context.error("Invalid download url: " + value + " provided. It must end with a /. Eg: " + value + "/");
				return false;
			}
			final URL url;
			try {
				url = new URL(value);
			} catch (MalformedURLException e) {
				context.error("Unable to parse provided download url: " + value + ". " + e.getMessage());
				return false;
			}
			try {
				URLConnection conn = url.openConnection();
				conn.connect();
			} catch (IOException e) {
				context.error("Unable to connect to server at url: " + value + ". " + e.getMessage());
				return false;
			}
		}
		return true;
	}

	@Override
	public void run(final Context context) {
	}

	@Override
	public String getShortDescription() {
		return "Download library dependencies if not available";
	}

	@Override
	public String getDetailedDescription() {
		return "Always download missing dependencies.\n" +
				"Dependencies will be checked for latest version.\n" +
				"Dependencies will be downloaded through Maven, DSL Platform website or custom URL specified.\n" +
				"Custom download URL can be specified; default URL = https://tools.dsl-platform.com/\n\n" +
				"Example:\n" +
				"\tdownload\n" +
				"\tdownload=http://company.domain/dsl-platform/";
	}
}
