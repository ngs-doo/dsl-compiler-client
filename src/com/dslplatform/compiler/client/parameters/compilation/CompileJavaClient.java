package com.dslplatform.compiler.client.parameters.compilation;

import com.dslplatform.compiler.client.DslServer;
import com.dslplatform.compiler.client.Either;
import com.dslplatform.compiler.client.InputParameter;
import com.dslplatform.compiler.client.Utils;
import com.dslplatform.compiler.client.parameters.Dependencies;
import com.dslplatform.compiler.client.parameters.Maven;
import com.dslplatform.compiler.client.parameters.Prompt;
import com.dslplatform.compiler.client.parameters.TempPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.Map;

public class CompileJavaClient implements CompileAction {

	@Override
	public boolean check(final Map<InputParameter, String> parameters) {
		final File depsRoot = Dependencies.getDependenciesRoot(parameters);
		final File javaDeps = new File(depsRoot.getAbsolutePath() + "/java_client");
		if (!javaDeps.exists()) {
			if (!javaDeps.mkdirs()) {
				System.out.println("Failed to create java dependency folder: " + javaDeps.getAbsolutePath());
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
			System.out.println("Java client not found in: " + javaDeps.getAbsolutePath());
			if (!parameters.containsKey(InputParameter.DOWNLOAD)) {
				if (!Prompt.canUsePrompt()) {
					System.out.println("Download option not enabled. Enable download option, change dependencies path or place Java client files in specified folder.");
					return false;
				}
				System.out.print("Do you wish to download latest Java client version from the Internet (y/N):");
				final String answer = System.console().readLine();
				if (!"y".equalsIgnoreCase(answer)) {
					System.exit(0);
				}
			}
			final Either<String> tryMaven = Maven.findMaven(parameters);
			if (!tryMaven.isSuccess()) {
				return downloadZip(javaDeps);
			}
			System.out.println("Downloading Java client from Sonatype...");
			try {
				//TODO: change after jar unification
				final URL maven = new URL("https://oss.sonatype.org/content/repositories/releases/com/dslplatform/dsl-client-core/maven-metadata.xml");
				final Either<Document> doc = Utils.readXml(maven.openConnection().getInputStream());
				if (!doc.isSuccess()) {
					System.out.println("Error downloading library info from Sonatype.");
					System.out.println(doc.whyNot());
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
				System.out.println("Compiling Java client library...");
				final Either<Utils.CommandResult> gatherDeps = Utils.runCommand(mvnCmd, pomFile.getParentFile());
				if (!gatherDeps.isSuccess()) {
					System.out.println("Error gathering dependencies with Maven.");
					System.out.println(gatherDeps.whyNot());
					return promptForAlternative(javaDeps, parameters);
				}
				final String result = gatherDeps.get().output + gatherDeps.get().error;
				if (!result.contains("BUILD SUCCESSFUL")) {
					System.out.println("Maven error during dependency download.");
					System.out.println(result);
					return promptForAlternative(javaDeps, parameters);
				}
			} catch (Exception ex) {
				System.out.println("Unable to download Java client from Sonatype.");
				System.out.println(ex.getMessage());
				return promptForAlternative(javaDeps, parameters);
			}
		}
		return true;
	}

	private static boolean promptForAlternative(final File javaDeps, final Map<InputParameter, String> parameters) {
		final String answer;
		if (!parameters.containsKey(InputParameter.DOWNLOAD)) {
			if (!Prompt.canUsePrompt()) {
				System.exit(0);
			}
			System.out.println();
			System.out.print("Try alternative download from DSL Platform (y/N):");
			answer = System.console().readLine();
		} else {
			answer = "y";
		}
		if ("y".equalsIgnoreCase(answer)) {
			return downloadZip(javaDeps);
		} else {
			System.exit(0);
		}
		return false;
	}

	private static boolean downloadZip(final File javaDeps) {
		try {
			System.out.println("Downloading Java client from DSL Platform...");
			DslServer.downloadAndUnpack("java-client", javaDeps);
		} catch (Exception ex) {
			System.out.println("Error downloading jar dependencies from DSL Platform.");
			System.out.println(ex.getMessage());
			return false;
		}
		return true;
	}

	@Override
	public void compile(final File path, final Map<InputParameter, String> parameters) {
		final File depsRoot = Dependencies.getDependenciesRoot(parameters);
		final File javaDeps = new File(depsRoot.getAbsolutePath() + "/java_client");
		final File model = new File("./generated-model.jar");
		final Either<String> compilation =
				JavaCompilation.compile(
						javaDeps,
						new File(TempPath.getTempPath(), "Java"),
						model,
						parameters);
		if (!compilation.isSuccess()) {
			System.out.println("Error during Java client library compilation.");
			System.out.println(compilation.whyNot());
			System.exit(0);
		}
		if (model.exists()) {
			System.out.println("Compiled Java client library to: " + model.getAbsolutePath());
		} else {
			System.out.println("Can't seem to find compiled Java client library: " + model.getAbsolutePath());
			System.out.println(compilation.get());
			System.exit(0);
		}
	}
}
