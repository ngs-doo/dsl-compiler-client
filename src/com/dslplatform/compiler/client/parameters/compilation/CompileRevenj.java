package com.dslplatform.compiler.client.parameters.compilation;

import com.dslplatform.compiler.client.*;
import com.dslplatform.compiler.client.parameters.Dependencies;
import com.dslplatform.compiler.client.parameters.TempPath;

import javax.net.ssl.HttpsURLConnection;
import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;

public class CompileRevenj implements CompileAction {

	@Override
	public boolean check(final Context context) {
		final File depsRoot = Dependencies.getDependenciesRoot(context);
		final File revenjDeps = new File(depsRoot.getAbsolutePath() + "/revenj");
		if (!revenjDeps.exists()) {
			if (!revenjDeps.mkdirs()) {
				context.error("Failed to create Revenj dependencies folder: " + revenjDeps.getAbsolutePath());
				return false;
			}
		}
		final File[] found = revenjDeps.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".dll");
			}
		});
		if (found.length == 0) {
			context.error("Revenj dependencies not found in: " + revenjDeps.getAbsolutePath());
			if (!context.contains(InputParameter.DOWNLOAD)) {
				if (!context.canInteract()) {
					context.error("Download option not enabled. Enable download option, change dependencies path or place Revenj files in specified folder.");
					return false;
				}
				final String answer = context.ask("Do you wish to download latest Revenj version from the Internet (y/N):");
				if (!"y".equalsIgnoreCase(answer)) {
					System.exit(0);
				}
			}
			try {
				context.log("Downloading Revenj from Github...");
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
				Utils.unpackZip(revenjDeps, httpServer.openConnection().getInputStream());
			} catch (Exception ex) {
				context.error("Unable to download Revenj from Github.");
				context.error(ex);
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
					try {
						context.log("Downloading Revenj from DSL Platform...");
						DslServer.downloadAndUnpack("server", revenjDeps);
					} catch (Exception ex2) {
						context.error("Unable to download Revenj from DSL Platform.");
						context.error(ex);
						return false;
					}
				} else {
					System.exit(0);
				}
			}
		}
		return true;
	}

	@Override
	public void compile(final File path, final Context context) {
		final File depsRoot = Dependencies.getDependenciesRoot(context);
		final File revenjDeps = new File(depsRoot.getAbsolutePath() + "/revenj");
		final File model = new File("./GeneratedModel.dll");
		final Either<String> compilation =
				DotNetCompilation.compile(
						new String[]{
								"System.dll",
								"System.Core.dll",
								"System.Dynamic.dll",
								"System.ComponentModel.Composition.dll",
								"System.Configuration.dll",
								"System.Data.dll",
								"System.Drawing.dll",
								"System.Xml.dll",
								"System.Xml.Linq.dll",
								"System.Runtime.Serialization.dll"},
						revenjDeps,
						new File(TempPath.getTempPath(context), "CSharpServer"),
						model,
						context);
		if (!compilation.isSuccess()) {
			context.error("Error during Revenj library compilation.");
			context.error(compilation.whyNot());
			System.exit(0);
		}
		if (model.exists()) {
			context.log("Compiled Revenj library to: " + model.getAbsolutePath());
		} else {
			context.error("Can't seem to find compiled Revenj library: " + model.getAbsolutePath());
			context.log(compilation.get());
			System.exit(0);
		}
	}
}
