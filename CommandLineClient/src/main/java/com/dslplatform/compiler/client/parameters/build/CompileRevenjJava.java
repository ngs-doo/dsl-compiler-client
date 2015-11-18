package com.dslplatform.compiler.client.parameters.build;

import com.dslplatform.compiler.client.*;
import com.dslplatform.compiler.client.parameters.Dependencies;
import com.dslplatform.compiler.client.parameters.Download;
import com.dslplatform.compiler.client.parameters.Namespace;

import java.io.File;
import java.util.*;

public class CompileRevenjJava implements BuildAction {

	private final String id;
	private final String zip;
	private final String maven;

	public CompileRevenjJava(
			final String id,
			final String zip,
			final String maven) {
		this.id = id;
		this.zip = zip;
		this.maven = maven;
	}

	@Override
	public boolean check(final Context context) throws ExitException {
		return Download.checkJars(context, "Revenj Java server", zip, id, "org/revenj", maven);
	}

	@Override
	public void build(final File sources, final Context context) throws ExitException {
		final File libDeps = Dependencies.getDependencies(context, "Revenj.Java", id);
		final String customJar = context.get(id);
		final File model = new File(customJar != null ? customJar : "./generated-server-model.jar");
		final String namespace = context.get(Namespace.INSTANCE);
		final String bootClass = namespace != null && namespace.length() > 0 ? namespace + ".Boot" : "Boot";
		//TODO: not sure if service should be registered without namespace
		final Map<String, List<String>> services = new HashMap<String, List<String>>();
		services.put("org.revenj.extensibility.SystemAspect", Collections.singletonList(bootClass));
		final Either<String> compilation = JavaCompilation.compile("revenj", libDeps, sources, model, services, context);
		if (!compilation.isSuccess()) {
			context.error("Error during Revenj.Java library compilation.");
			context.error(compilation.whyNot());
			throw new ExitException();
		}
		if (model.exists()) {
			context.show("Compiled Revenj.Java library to: " + model.getAbsolutePath());
		} else {
			context.error("Can't seem to find compiled Revenj.Java library: " + model.getAbsolutePath());
			context.log(compilation.get());
			throw new ExitException();
		}
	}
}
