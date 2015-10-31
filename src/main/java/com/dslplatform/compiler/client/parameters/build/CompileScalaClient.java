package com.dslplatform.compiler.client.parameters.build;

import com.dslplatform.compiler.client.*;
import com.dslplatform.compiler.client.parameters.Dependencies;
import com.dslplatform.compiler.client.parameters.Download;

import java.io.File;

public class CompileScalaClient implements BuildAction {

	private final String name;
	private final String zip;
	private final String library;
	private final String maven;
	private final String jar;

	public CompileScalaClient(
			final String name,
			final String zip,
			final String library,
			final String maven,
			final String jar) {
		this.name = name;
		this.zip = zip;
		this.library = library;
		this.maven = maven;
		this.jar = jar;
	}

	public CompileScalaClient(
			final String name,
			final String library,
			final String jar) {
		this.name = name;
		this.zip = null;
		this.library = library;
		this.maven = null;
		this.jar = jar;
	}

	@Override
	public boolean check(final Context context) throws ExitException {
		if (zip == null && maven == null) {
			return true;
		}
		return Download.checkJars(context, name, zip, library, "com/dslplatform", maven);
	}

	@Override
	public void build(final File sources, final Context context) throws ExitException {
		final File libDeps = Dependencies.getDependencies(context, name, library);
		final String customJar = context.get(library);
		final File model = new File(customJar != null ? customJar : jar);
		final Either<String> compilation = ScalaCompilation.compile(library, libDeps, sources, model, context);
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
