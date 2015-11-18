package com.dslplatform.compiler.client.parameters.build;

import com.dslplatform.compiler.client.*;
import com.dslplatform.compiler.client.parameters.Dependencies;
import com.dslplatform.compiler.client.parameters.Download;

import javax.net.ssl.HttpsURLConnection;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;

public class CompileRevenjNet implements BuildAction {

	private final String id;
	private final String additionalZip;

	public CompileRevenjNet(final String id, final String additionalZip) {
		this.id = id;
		this.additionalZip = additionalZip;
	}

	@Override
	public boolean check(final Context context) throws ExitException {
		final File revenjDeps = Dependencies.getDependencies(context, "Revenj.NET", id);
		final File[] found = revenjDeps.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".dll");
			}
		});
		if (found.length == 0) {
			context.error("Revenj.NET dependencies not found in: " + revenjDeps.getAbsolutePath());
			return downloadFromGithub(context, "Revenj.NET", "revenj-core", additionalZip, revenjDeps);
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
		final File revenjDeps = Dependencies.getDependencies(context, "Revenj.NET", id);
		final String customDll = context.get(id);
		final File model = new File(customDll != null ? customDll : "./GeneratedModel.dll");
		context.show("Compiling Revenj.NET library...");
		final Either<String> compilation =
				DotNetCompilation.compile(DEPENDENCIES, revenjDeps, sources, model, context, false);
		if (!compilation.isSuccess()) {
			context.error("Error during Revenj.NET library compilation.");
			context.error(compilation.whyNot());
			throw new ExitException();
		}
		if (model.exists()) {
			context.show("Compiled Revenj.NET library to: " + model.getAbsolutePath());
		} else {
			context.error("Can't seem to find compiled Revenj.NET library: " + model.getAbsolutePath());
			context.log(compilation.get());
			throw new ExitException();
		}
	}

	private static boolean downloadFromGithub(
			final Context context,
			final String name,
			final String zip,
			final String additionalZip,
			final File target) throws ExitException {
		if (!context.contains(Download.INSTANCE)) {
			if (!context.canInteract()) {
				context.error("Download option not enabled.\n" +
						"Enable download option, change dependencies path or place " + name + " files in specified folder: " + target.getAbsolutePath());
				throw new ExitException();
			}
			final String answer = context.ask("Do you wish to download latest Revenj.NET version from the Internet (y/N):");
			if (!"y".equalsIgnoreCase(answer)) {
				throw new ExitException();
			}
		}
		try {
			context.show("Downloading " + name + " from GitHub...");
			final URL latest = new URL("https://github.com/ngs-doo/revenj/releases/latest");
			final HttpsURLConnection conn = (HttpsURLConnection) latest.openConnection();
			conn.setInstanceFollowRedirects(false);
			conn.setUseCaches(false);
			conn.connect();
			final String tag;
			if (conn.getResponseCode() != 302) {
				context.error("Error downloading " + name + " from GitHub. Will continue with tag 1.2.1. Expecting redirect. Got: " + conn.getResponseCode());
				tag = "1.2.1";
			} else {
				final String redirect = conn.getHeaderField("Location");
				tag = redirect.substring(redirect.lastIndexOf('/') + 1);
			}
			final URL coreUrl = new URL("https://github.com/ngs-doo/revenj/releases/download/" + tag + "/" + zip + ".zip");
			Utils.unpackZip(context, target, coreUrl);
			if (additionalZip != null) {
				final URL zipUrl = new URL("https://github.com/ngs-doo/revenj/releases/download/" + tag + "/" + additionalZip + ".zip");
				Utils.unpackZip(context, target, zipUrl);
			}
		} catch (IOException ex) {
			context.error("Unable to download " + name + " from GitHub.");
			context.error(ex);
			return false;
		}
		return true;
	}
}
