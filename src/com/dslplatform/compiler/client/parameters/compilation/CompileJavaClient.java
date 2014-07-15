package com.dslplatform.compiler.client.parameters.compilation;

import com.dslplatform.compiler.client.InputParameter;
import com.dslplatform.compiler.client.parameters.Dependencies;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Map;

public class CompileJavaClient implements CompileAction {
	@Override
	public void compile(final File path, final Map<InputParameter, String> parameters) {
		final File depsRoot = Dependencies.getDependenciesRoot(parameters);
		final File javaDeps = new File(depsRoot.getAbsolutePath() + "/java_client");
		if (!javaDeps.exists()) {
			javaDeps.mkdirs();
		}
		final File[] found = javaDeps.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".jar");
			}
		});
		if (found.length == 0) {
			System.out.println("Java client not found in: " + javaDeps.getAbsolutePath());
			if(parameters.containsKey(InputParameter.DOWNLOAD)) {
				System.out.println("Downloading dsl client for Java...");
			} else {
				System.out.println("Download option not enabled. Enable download option, change dependencies path or place java client files in specified folder.");
				System.exit(0);
			}
		}
	}
}
