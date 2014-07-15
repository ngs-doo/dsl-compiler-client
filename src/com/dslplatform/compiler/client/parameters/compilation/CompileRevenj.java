package com.dslplatform.compiler.client.parameters.compilation;

import com.dslplatform.compiler.client.InputParameter;
import com.dslplatform.compiler.client.Utils;
import com.dslplatform.compiler.client.parameters.Dependencies;

import javax.net.ssl.HttpsURLConnection;
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
			if(parameters.containsKey(InputParameter.DOWNLOAD)) {
				System.out.println("Downloading Revenj from Github...");
				try {
					final URL latest = new URL("https://github.com/ngs-doo/revenj/releases/latest");
					final HttpsURLConnection conn = (HttpsURLConnection)latest.openConnection();
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
				}catch (Exception ex) {
					System.out.println("Unable to download Revenj from Github.");
					System.out.println(ex.getMessage());
					System.exit(0);
				}
			} else {
				System.out.println("Download option not enabled. Enable download option, change dependencies path or place Revenj files in specified folder.");
				System.exit(0);
			}
		}
	}
}
