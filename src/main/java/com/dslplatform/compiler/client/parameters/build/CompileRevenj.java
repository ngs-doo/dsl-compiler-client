package com.dslplatform.compiler.client.parameters.build;

import com.dslplatform.compiler.client.*;
import com.dslplatform.compiler.client.parameters.Dependencies;
import com.dslplatform.compiler.client.parameters.Download;

import javax.net.ssl.HttpsURLConnection;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;

public class CompileRevenj implements BuildAction {

	@Override
	public boolean check(final Context context) throws ExitException {
		final File revenjDeps = Dependencies.getDependencies(context, "Revenj", "revenj");
		final File[] found = revenjDeps.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".dll");
			}
		});
		if (found.length == 0) {
			context.error("Revenj dependencies not found in: " + revenjDeps.getAbsolutePath());
			if (!context.contains(Download.INSTANCE)) {
				if (!context.canInteract()) {
					context.error("Download option not enabled. Enable download option, change dependencies path or place Revenj files in specified folder.");
					throw new ExitException();
				}
				final String answer = context.ask("Do you wish to download latest Revenj version from the Internet (y/N):");
				if (!"y".equalsIgnoreCase(answer)) {
					throw new ExitException();
				}
			}
			try {
				context.show("Downloading Revenj from Github...");
				final URL latest = new URL("https://github.com/ngs-doo/revenj/releases/latest");
				final HttpsURLConnection conn = (HttpsURLConnection) latest.openConnection();
				conn.setInstanceFollowRedirects(false);
				conn.setUseCaches(false);
				conn.connect();
				if (conn.getResponseCode() != 302) {
					context.error("Error downloading Revenj from Github. Expecting redirect. Got: " + conn.getResponseCode());
					return false;
				}
				final String redirect = conn.getHeaderField("Location");
				final String tag = redirect.substring(redirect.lastIndexOf('/') + 1);
				final URL httpServer = new URL("https://github.com/ngs-doo/revenj/releases/download/" + tag + "/http-server.zip");
				Utils.unpackZip(context, revenjDeps, httpServer);
			} catch (IOException ex) {
				context.error("Unable to download Revenj from Github.");
				context.error(ex);
				final String answer;
				if (!context.contains(Download.INSTANCE)) {
					if (!context.canInteract()) {
						throw new ExitException();
					}
					answer = context.ask("Try alternative download from DSL Platform (y/N):");
				} else {
					answer = "y";
				}
				if ("y".equalsIgnoreCase(answer)) {
					try {
						context.show("Downloading Revenj from DSL Platform...");
						DslServer.downloadAndUnpack(context, "server", revenjDeps);
					} catch (IOException ex2) {
						context.error("Unable to download Revenj from DSL Platform.");
						context.error(ex);
						return false;
					}
				} else {
					throw new ExitException();
				}
			}
		}
		return true;
	}

	private static final String[] DEPENDENCIES = {
			"System.dll",
			"System.Core.dll",
			"System.Dynamic.dll",
			"System.ComponentModel.Composition.dll",
			"System.Configuration.dll",
			"System.Data.dll",
			"System.Drawing.dll",
			"System.Xml.dll",
			"System.Xml.Linq.dll",
			"System.Runtime.Serialization.dll"
	};

	@Override
	public void build(final File sources, final Context context) throws ExitException {
		final File revenjDeps = Dependencies.getDependencies(context, "Revenj", "revenj");
		final String customDll = context.get("revenj");
		final File model = new File(customDll != null ? customDll : "./GeneratedModel.dll");
		context.show("Compiling Revenj library...");
		final Either<String> compilation =
				DotNetCompilation.compile(DEPENDENCIES, revenjDeps, sources, model, context, false);
		if (!compilation.isSuccess()) {
			context.error("Error during Revenj library compilation.");
			context.error(compilation.whyNot());
			throw new ExitException();
		}
		if (model.exists()) {
			context.show("Compiled Revenj library to: " + model.getAbsolutePath());
		} else {
			context.error("Can't seem to find compiled Revenj library: " + model.getAbsolutePath());
			context.log(compilation.get());
			throw new ExitException();
		}
	}
}
