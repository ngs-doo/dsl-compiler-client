package com.dslplatform.compiler.client.parameters.build;

import com.dslplatform.compiler.client.*;
import com.dslplatform.compiler.client.parameters.Dependencies;

import javax.net.ssl.HttpsURLConnection;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public class CompileCsClient implements BuildAction {

	private final String name;
	private final String zip;
	private final String library;
	private final String dll;
	private final String[] dependencies;

	public CompileCsClient(
			final String name,
			final String zip,
			final String library,
			final String dll,
			final String[] dependencies) {
		this.name = name;
		this.zip = zip;
		this.library = library;
		this.dll = dll;
		this.dependencies = dependencies;
	}

	@Override
	public boolean check(final Context context) throws ExitException {
		final File libDeps = Dependencies.getDependencies(context, name, library);
		final File[] found = libDeps.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".dll");
			}
		});
		if (found.length == 0) {
			context.error(name + " dependencies not found in: " + libDeps.getAbsolutePath());
			if (!context.contains(InputParameter.DOWNLOAD)) {
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
				context.show("Downloading " + name + " from DSL Platform...");
				DslServer.downloadAndUnpack(context, zip, libDeps);
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
		final Either<String> compilation = DotNetCompilation.compile(dependencies, libDeps, sources, model, context);
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
