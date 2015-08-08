package com.dslplatform.compiler.client.parameters.build;

import com.dslplatform.compiler.client.*;
import com.dslplatform.compiler.client.parameters.Dependencies;

import java.io.File;
import java.io.FilenameFilter;

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
			return DslServer.downloadFromGithubOrPlatform(context, "Revenj.NET", "revenj-core", additionalZip, revenjDeps);
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
}
