package com.dslplatform.compiler.client.parameters.build;

import com.dslplatform.compiler.client.*;
import com.dslplatform.compiler.client.parameters.Dependencies;
import com.dslplatform.compiler.client.parameters.Maven;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;

public class CompileScalaClient implements BuildAction {

	@Override
	public boolean check(final Context context) throws ExitException {
		final File depsRoot = Dependencies.getDependenciesRoot(context);
		final File scalaDeps = new File(depsRoot.getAbsolutePath() + "/scala_client");
		if (!scalaDeps.exists()) {
			if (!scalaDeps.mkdirs()) {
				context.error("Failed to create Scala client dependency folder: " + scalaDeps.getAbsolutePath());
				return false;
			}
		}
		final File[] found = scalaDeps.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".jar");
			}
		});
		if (found.length == 0) {
			context.error("Scala client not found in: " + scalaDeps.getAbsolutePath());
			if (!context.contains(InputParameter.DOWNLOAD)) {
				if (!context.canInteract()) {
					context.error("Download option not enabled. Enable download option, change dependencies path or place Scala client files in specified folder.");
					return false;
				}
				final String answer = context.ask("Do you wish to download latest Scala client version from the Internet (y/N):");
				if (!"y".equalsIgnoreCase(answer)) {
					throw new ExitException();
				}
			}
			final Either<String> tryMaven = Maven.findMaven(context);
			if (!tryMaven.isSuccess()) {
				return downloadZip(scalaDeps, context);
			}
			context.show("Downloading Scala client from Sonatype...");
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
				final File pomFile = new File(scalaDeps, "dsl-client-http-apache-" + version + ".pom");
				Utils.downloadFile(pomFile, pomUrl);
				final URL jarUrl = new URL(sharedUrl + "http-apache/" + version + "/dsl-client-http-apache-" + version + ".jar");
				Utils.downloadFile(new File(scalaDeps, "dsl-client-http-apache-" + version + ".jar"), jarUrl);
				final String mvnCmd = tryMaven.get() + " dependency:copy-dependencies " +
						"\"-DoutputDirectory=" + scalaDeps.getAbsolutePath() + "\" \"-f=" + pomFile.getAbsolutePath() + "\"";
				context.show("Compiling Scala client library...");
				final Either<Utils.CommandResult> gatherDeps = Utils.runCommand(mvnCmd, pomFile.getParentFile());
				if (!gatherDeps.isSuccess()) {
					context.error("Error gathering dependencies with Maven.");
					context.error(gatherDeps.whyNot());
					return promptForAlternative(scalaDeps, context);
				}
				final String result = gatherDeps.get().output + gatherDeps.get().error;
				if (!result.contains("BUILD SUCCESSFUL")) {
					context.error("Maven error during dependency download.");
					context.show(result);
					return promptForAlternative(scalaDeps, context);
				}
			} catch (IOException ex) {
				context.error("Unable to download Scala client from Sonatype.");
				context.error(ex);
				return promptForAlternative(scalaDeps, context);
			}
		}
		return true;
	}

	private static boolean promptForAlternative(final File scalaDeps, final Context context) throws ExitException {
		final String answer;
		if (!context.contains(InputParameter.DOWNLOAD)) {
			if (!context.canInteract()) {
				throw new ExitException();
			}
			answer = context.ask("Try alternative download from DSL Platform (y/N):");
		} else {
			answer = "y";
		}
		if (!"y".equalsIgnoreCase(answer)) {
			throw new ExitException();
		}
		return downloadZip(scalaDeps, context);
	}

	private static boolean downloadZip(final File scalaDeps, final Context context) {
		try {
			context.show("Downloading Scala client from DSL Platform...");
			DslServer.downloadAndUnpack("scala-client", scalaDeps);
		} catch (IOException ex) {
			context.error("Error downloading jar dependencies from DSL Platform.");
			context.error(ex);
			return false;
		}
		return true;
	}

	@Override
	public void build(final File sources, final Context context) throws ExitException {
		final File depsRoot = Dependencies.getDependenciesRoot(context);
		final File scalaDeps = new File(depsRoot.getAbsolutePath() + "/scala_client");
		final File model = new File("./generated-model-scala.jar");
		final Either<String> compilation = ScalaCompilation.compile(scalaDeps, sources, model, context);
		if (!compilation.isSuccess()) {
			context.error("Error during Scala client library compilation.");
			context.error(compilation.whyNot());
			throw new ExitException();
		}
		if (model.exists()) {
			context.show("Compiled Scala client library to: " + model.getAbsolutePath());
		} else {
			context.error("Can't seem to find compiled Scala client library: " + model.getAbsolutePath());
			context.show(compilation.get());
			throw new ExitException();
		}
	}
}
