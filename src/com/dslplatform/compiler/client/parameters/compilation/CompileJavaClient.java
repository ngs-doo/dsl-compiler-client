package com.dslplatform.compiler.client.parameters.compilation;

import com.dslplatform.compiler.client.*;
import com.dslplatform.compiler.client.parameters.Dependencies;
import com.dslplatform.compiler.client.parameters.Maven;
import com.dslplatform.compiler.client.parameters.TempPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;

public class CompileJavaClient implements CompileAction {

	@Override
	public boolean check(final Context context) {
		final File depsRoot = Dependencies.getDependenciesRoot(context);
		final File javaDeps = new File(depsRoot.getAbsolutePath() + "/java_client");
		if (!javaDeps.exists()) {
			if (!javaDeps.mkdirs()) {
				context.error("Failed to create java dependency folder: " + javaDeps.getAbsolutePath());
				return false;
			}
		}
		final File[] found = javaDeps.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".jar");
			}
		});
		if (found.length == 0) {
			context.error("Java client not found in: " + javaDeps.getAbsolutePath());
			if (!context.contains(InputParameter.DOWNLOAD)) {
				if (!context.canInteract()) {
					context.error("Download option not enabled. Enable download option, change dependencies path or place Java client files in specified folder.");
					return false;
				}
				final String answer = context.ask("Do you wish to download latest Java client version from the Internet (y/N):");
				if (!"y".equalsIgnoreCase(answer)) {
					System.exit(0);
				}
			}
			final Either<String> tryMaven = Maven.findMaven(context);
			if (!tryMaven.isSuccess()) {
				return downloadZip(javaDeps, context);
			}
			context.log("Downloading Java client from Sonatype...");
			try {
				//TODO: change after jar unification
				final URL maven = new URL("https://oss.sonatype.org/content/repositories/releases/com/dslplatform/dsl-client-core/maven-metadata.xml");
				final Either<Document> doc = Utils.readXml(maven.openConnection().getInputStream());
				if (!doc.isSuccess()) {
					context.error("Error downloading library info from Sonatype.");
					context.error(doc.whyNot());
					return false;
				}
				final Element root = doc.get().getDocumentElement();
				final Element versioning = (Element) root.getElementsByTagName("versioning").item(0);
				final String version = versioning.getElementsByTagName("release").item(0).getTextContent();
				final String sharedUrl = "https://oss.sonatype.org/content/repositories/releases/com/dslplatform/dsl-client-";
				final URL pomUrl = new URL(sharedUrl + "http-apache/" + version + "/dsl-client-http-apache-" + version + ".pom");
				final File pomFile = new File(javaDeps, "dsl-client-http-apache-" + version + ".pom");
				Utils.downloadFile(pomFile, pomUrl);
				final URL jarUrl = new URL(sharedUrl + "http-apache/" + version + "/dsl-client-http-apache-" + version + ".jar");
				Utils.downloadFile(new File(javaDeps, "dsl-client-http-apache-" + version + ".jar"), jarUrl);
				final String mvnCmd = tryMaven.get() + " dependency:copy-dependencies " +
						"\"-DoutputDirectory=" + javaDeps.getAbsolutePath() + "\" \"-f=" + pomFile.getAbsolutePath() + "\"";
				context.log("Compiling Java client library...");
				final Either<Utils.CommandResult> gatherDeps = Utils.runCommand(mvnCmd, pomFile.getParentFile());
				if (!gatherDeps.isSuccess()) {
					context.error("Error gathering dependencies with Maven.");
					context.log(gatherDeps.whyNot());
					return promptForAlternative(javaDeps, context);
				}
				final String result = gatherDeps.get().output + gatherDeps.get().error;
				if (!result.contains("BUILD SUCCESSFUL")) {
					context.error("Maven error during dependency download.");
					context.log(result);
					return promptForAlternative(javaDeps, context);
				}
			} catch (Exception ex) {
				context.error("Unable to download Java client from Sonatype.");
				context.error(ex);
				return promptForAlternative(javaDeps, context);
			}
		}
		return true;
	}

	private static boolean promptForAlternative(final File javaDeps, final Context context) {
		final String answer;
		if (!context.contains(InputParameter.DOWNLOAD)) {
			if (!context.canInteract()) {
				System.exit(0);
			}
			answer = context.ask("Try alternative download from DSL Platform (y/N):");
		} else {
			answer = "y";
		}
		if ("y".equalsIgnoreCase(answer)) {
			return downloadZip(javaDeps, context);
		} else {
			System.exit(0);
		}
		return false;
	}

	private static boolean downloadZip(final File javaDeps, final Context context) {
		try {
			context.log("Downloading Java client from DSL Platform...");
			DslServer.downloadAndUnpack("java-client", javaDeps);
		} catch (Exception ex) {
			context.error("Error downloading jar dependencies from DSL Platform.");
			context.error(ex);
			return false;
		}
		return true;
	}

	@Override
	public void compile(final File path, final Context context) {
		final File depsRoot = Dependencies.getDependenciesRoot(context);
		final File javaDeps = new File(depsRoot.getAbsolutePath() + "/java_client");
		final File model = new File("./generated-model.jar");
		final Either<String> compilation =
				JavaCompilation.compile(
						javaDeps,
						new File(TempPath.getTempPath(context), "Java"),
						model,
						context);
		if (!compilation.isSuccess()) {
			context.error("Error during Java client library compilation.");
			context.error(compilation.whyNot());
			System.exit(0);
		}
		if (model.exists()) {
			context.log("Compiled Java client library to: " + model.getAbsolutePath());
		} else {
			context.error("Can't seem to find compiled Java client library: " + model.getAbsolutePath());
			context.log(compilation.get());
			System.exit(0);
		}
	}
}
