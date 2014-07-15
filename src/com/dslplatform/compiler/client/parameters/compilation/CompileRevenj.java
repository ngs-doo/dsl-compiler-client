package com.dslplatform.compiler.client.parameters.compilation;

import com.dslplatform.compiler.client.InputParameter;
import com.dslplatform.compiler.client.Utils;
import com.dslplatform.compiler.client.parameters.Dependencies;
import com.dslplatform.compiler.client.parameters.Prompt;

import javax.net.ssl.HttpsURLConnection;
import java.io.Console;
import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.Map;

public class CompileRevenj implements CompileAction {
	@Override
	public void compile(final File path, final Map<InputParameter, String> parameters) {
		final File depsRoot = Dependencies.getDependenciesRoot(parameters);
		final File revenjDeps = new File(depsRoot.getAbsolutePath() + "/revenj");
		if (!revenjDeps.exists()) {
			if (!revenjDeps.mkdirs()) {
				System.out.println("Failed to create Revenj dependencies folder: " + revenjDeps.getAbsolutePath());
				System.exit(0);
			}
		}
		final File[] found = revenjDeps.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.equals("Revenj.Http.exe");
			}
		});
		if (found.length == 0) {
			System.out.println("Revenj not found in: " + revenjDeps.getAbsolutePath());
			if (!parameters.containsKey(InputParameter.DOWNLOAD)) {
				if (!Prompt.canUsePrompt()) {
					System.out.println("Download option not enabled. Enable download option, change dependencies path or place Revenj files in specified folder.");
					System.exit(0);
				}
				System.out.println("Do you wish to download latest Revenj version from the Internet (y/N):");
				final String answer = System.console().readLine();
				if (!"y".equalsIgnoreCase(answer)) {
					System.exit(0);
				}
			}
			try {
				System.out.println("Downloading Revenj from Github...");
				final URL latest = new URL("https://github.com/ngs-doo/revenj/releases/latest");
				final HttpsURLConnection conn = (HttpsURLConnection) latest.openConnection();
				conn.setInstanceFollowRedirects(false);
				conn.setUseCaches(false);
				conn.connect();
				if (conn.getResponseCode() != 302) {
					System.out.println("Error downloading Revenj from Github. Expecting redirect. Got: " + conn.getResponseCode());
					System.exit(0);
				}
				final String redirect = conn.getHeaderField("Location");
				final String tag = redirect.substring(redirect.lastIndexOf('/') + 1);
				final URL httpServer = new URL("https://github.com/ngs-doo/revenj/releases/download/" + tag + "/http-server.zip");
				Utils.unpackZip(revenjDeps, httpServer.openConnection().getInputStream());
			} catch (Exception ex) {
				System.out.println("Unable to download Revenj from Github.");
				System.out.println(ex.getMessage());
				final Console console = System.console();
				if (console == null) {
					System.exit(0);
				}
				System.out.print("Retry download from DSL Platform (y/N):");
				final String answer = console.readLine();
				if ("y".equalsIgnoreCase(answer)) {
					try {
						System.out.println("Downloading Revenj from DSL Platform...");
						final URL server = new URL("https://compiler.dsl-platform.com:8443/platform/download/server.zip");
						Utils.unpackZip(revenjDeps, server.openConnection().getInputStream());
					} catch (Exception ex2) {
						System.out.println("Unable to download Revenj from DSL Platform.");
						System.out.println(ex.getMessage());
						System.exit(0);
					}
				} else {
					System.exit(0);
				}
			}
		}
	}
}
