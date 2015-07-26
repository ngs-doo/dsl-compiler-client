package com.dslplatform.compiler.client.parameters.build;

import com.dslplatform.compiler.client.*;
import com.dslplatform.compiler.client.parameters.Dependencies;
import com.dslplatform.compiler.client.parameters.Download;

import javax.net.ssl.HttpsURLConnection;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;

public class CompileRevenjJava implements BuildAction {

	private final String id;

	public CompileRevenjJava(final String id) {
		this.id = id;
	}

	@Override
	public boolean check(final Context context) throws ExitException {
		final File revenjDeps = Dependencies.getDependencies(context, "Revenj.Java", id);
		final File[] found = revenjDeps.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".jar");
			}
		});
		if (found.length == 0) {
			context.error("Revenj.Java dependencies not found in: " + revenjDeps.getAbsolutePath());
			if (!context.contains(Download.INSTANCE)) {
				if (!context.canInteract()) {
					context.error("Download option not enabled. Enable download option, change dependencies path or place Revenj.Java files in specified folder.");
					throw new ExitException();
				}
				final String answer = context.ask("Do you wish to download latest Revenj.Java version from the Internet (y/N):");
				if (!"y".equalsIgnoreCase(answer)) {
					throw new ExitException();
				}
			}
			/*
			try {
				context.show("Downloading Revenj.Java from GitHub...");
				final URL latest = new URL("https://github.com/ngs-doo/revenj/releases/latest");
				final HttpsURLConnection conn = (HttpsURLConnection) latest.openConnection();
				conn.setInstanceFollowRedirects(false);
				conn.setUseCaches(false);
				conn.connect();
				if (conn.getResponseCode() != 302) {
					context.error("Error downloading Revenj.NET from GitHub. Expecting redirect. Got: " + conn.getResponseCode());
					return false;
				}
				final String redirect = conn.getHeaderField("Location");
				final String tag = redirect.substring(redirect.lastIndexOf('/') + 1);
				final URL httpServer = new URL("https://github.com/ngs-doo/revenj/releases/download/" + tag + "/jvm-server.zip");
				Utils.unpackZip(context, revenjDeps, httpServer);
			} catch (IOException ex) {
				context.error("Unable to download Revenj.NET from GitHub.");
				context.error(ex);*/
				final String answer;
				if (!context.contains(Download.INSTANCE)) {
					if (!context.canInteract()) {
						throw new ExitException();
					}
					//answer = context.ask("Try alternative download from DSL Platform (y/N):");
					answer = context.ask("Download from DSL Platform (y/N):");
				} else {
					answer = "y";
				}
				if ("y".equalsIgnoreCase(answer)) {
					try {
						context.show("Downloading Revenj.Java from DSL Platform...");
						DslServer.downloadAndUnpack(context, "revenj-java", revenjDeps);
					} catch (IOException ex2) {
						context.error("Unable to download Revenj.Java from DSL Platform.");
						context.error(ex2);
						return false;
					}
				} else {
					throw new ExitException();
				}
			//}
		}
		return true;
	}

	@Override
	public void build(final File sources, final Context context) throws ExitException {
		final File libDeps = Dependencies.getDependencies(context, "Revenj.Java", id);
		final String customJar = context.get(id);
		final File model = new File(customJar != null ? customJar : "./generated-server-model.jar");
		final Either<String> compilation = JavaCompilation.compile("revenj", libDeps, sources, model, context);
		if (!compilation.isSuccess()) {
			context.error("Error during Revenj.Java library compilation.");
			context.error(compilation.whyNot());
			throw new ExitException();
		}
		if (model.exists()) {
			context.show("Compiled Revenj.Java library to: " + model.getAbsolutePath());
		} else {
			context.error("Can't seem to find compiled Revenj.Java library: " + model.getAbsolutePath());
			context.log(compilation.get());
			throw new ExitException();
		}
	}
}
