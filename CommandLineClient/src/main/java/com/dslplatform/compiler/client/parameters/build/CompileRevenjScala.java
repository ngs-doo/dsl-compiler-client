package com.dslplatform.compiler.client.parameters.build;

import com.dslplatform.compiler.client.Context;
import com.dslplatform.compiler.client.Either;
import com.dslplatform.compiler.client.ExitException;
import com.dslplatform.compiler.client.parameters.Dependencies;
import com.dslplatform.compiler.client.parameters.Download;
import com.dslplatform.compiler.client.parameters.Namespace;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompileRevenjScala implements BuildAction {

	private final String id;
	private final String zip;
	private final String[] maven;

	public CompileRevenjScala(
			final String id,
			final String zip,
			final String... maven) {
		this.id = id;
		this.zip = zip;
		this.maven = maven;
	}

	@Override
	public boolean check(final Context context) throws ExitException {
		return Download.checkJars(context, "Revenj Scala server", zip, id, "net/revenj", maven);
	}

	@Override
	public void build(final File sources, final Context context) throws ExitException {
		final File libDeps = Dependencies.getDependencies(context, "Revenj.Scala", id);
		final String customJar = context.get(id);
		final File model = new File(customJar != null ? customJar : "./generated-server-model-scala.jar");
		final String namespace = context.get(Namespace.INSTANCE);
		final String bootClass = namespace != null && namespace.length() > 0 ? namespace + ".Boot" : "Boot";
		//TODO: not sure if service should be registered without namespace
		final Map<String, List<String>> services = new HashMap<String, List<String>>();
		services.put("net.revenj.extensibility.SystemAspect", Collections.singletonList(bootClass));
		final Either<String> compilation = ScalaCompilation.compile("revenj", libDeps, sources, model, services, context);
		if (!compilation.isSuccess()) {
			context.error("Error during Revenj.Scala library compilation.");
			context.error(compilation.whyNot());
			throw new ExitException();
		}
		if (model.exists()) {
			context.show("Compiled Revenj.Scala library to: " + model.getAbsolutePath());
		} else {
			context.error("Can't seem to find compiled Revenj.Scala library: " + model.getAbsolutePath());
			context.log(compilation.get());
			throw new ExitException();
		}
	}
}
