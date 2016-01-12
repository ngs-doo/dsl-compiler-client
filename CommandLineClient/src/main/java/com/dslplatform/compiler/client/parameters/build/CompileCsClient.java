package com.dslplatform.compiler.client.parameters.build;

import com.dslplatform.compiler.client.*;
import com.dslplatform.compiler.client.parameters.Dependencies;
import com.dslplatform.compiler.client.parameters.Download;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

public class CompileCsClient implements BuildAction {

	private final String name;
	private final String zip;
	private final String library;
	private final String dll;
	private final String[] dependencies;
	private final boolean force32Bit;

	public CompileCsClient(
			final String name,
			final String zip,
			final String library,
			final String dll,
			final String[] dependencies,
			final boolean force32Bit) {
		this.name = name;
		this.zip = zip;
		this.library = library;
		this.dll = dll;
		this.dependencies = dependencies;
		this.force32Bit = force32Bit;
	}

	@Override
	public boolean check(final Context context) throws ExitException {
		if(force32Bit && !Utils.isWindows()) {
			context.error(name + " is only currently available on Windows.");
			throw new ExitException();
		}
		for (int i = 0; i < dependencies.length; i++) {
			if (dependencies[i].startsWith("gac/")) {
				final String name = dependencies[i].substring(4);
				final File assemblyPath = new File(new File(new File(System.getenv("WINDIR")), "Microsoft.NET"), "assembly");
				final File gac32 = new File(assemblyPath, "GAC_32");
				final File gacMsil = new File(assemblyPath, "GAC_MSIL");
				final File dp32 = new File(gac32, name);
				final File dpMsil = new File(gacMsil, name);
				final File dp = dp32.exists() ? dp32 : dpMsil;
				String[] versions = dp.list();
				if (versions == null || versions.length == 0) {
					context.error("Unable to find " + name + " in GAC. Looking in: " + dp);
					throw new ExitException();
				}
				final File actualFile = new File(new File(dp, versions[0]), name + ".dll");
				if (!actualFile.exists()) {
					context.error("Unable to find " + name + " in GAC. Looking in: " + actualFile);
					throw new ExitException();
				}
				dependencies[i] = actualFile.getAbsolutePath();
			}
		}
		final File libDeps = Dependencies.getDependencies(context, name, library);
		final File[] found = libDeps.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".dll");
			}
		});
		if (zip != null && found.length == 0) {
			context.error(name + " dependencies not found in: " + libDeps.getAbsolutePath());
			if (!context.contains(Download.INSTANCE)) {
				if (!context.canInteract()) {
					context.error("Download option not enabled. Enable download option, change dependencies path or place " + name + " files in specified folder.");
					throw new ExitException();
				}
				final String answer = context.ask("Do you wish to download latest " + name + " version from the Internet (y/N):");
				if (!"y".equalsIgnoreCase(answer)) {
					throw new ExitException();
				}
			}
			try {
				Utils.downloadAndUnpack(context, zip, libDeps);
			} catch (IOException ex) {
				context.error("Unable to download " + name + " from DSL Platform.");
				context.error(ex);
				return false;
			}
		}
		return true;
	}

	@Override
	public void build(final File sources, final Context context) throws ExitException {
		final File libDeps = Dependencies.getDependencies(context, name, library);
		final String customDll = context.get(library);
		final File model = new File(customDll != null ? customDll : dll);
		context.show("Compiling " + name + " library...");
		final Either<String> compilation =
				DotNetCompilation.compile(dependencies, libDeps, sources, model, context, force32Bit);
		if (!compilation.isSuccess()) {
			context.error("Error during " + name + " library compilation.");
			context.error(compilation.whyNot());
			throw new ExitException();
		}
		if (model.exists()) {
			context.show("Compiled " + name + " library to: " + model.getAbsolutePath());
		} else {
			context.error("Can't seem to find compiled " + name + " library: " + model.getAbsolutePath());
			context.log(compilation.get());
			throw new ExitException();
		}
	}
}
