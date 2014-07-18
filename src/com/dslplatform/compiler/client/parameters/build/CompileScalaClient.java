package com.dslplatform.compiler.client.parameters.build;

import com.dslplatform.compiler.client.*;
import com.dslplatform.compiler.client.parameters.Dependencies;
import com.dslplatform.compiler.client.parameters.Download;

import java.io.File;

public class CompileScalaClient implements BuildAction {

	@Override
	public boolean check(final Context context) throws ExitException {
		return Download.checkJars(context, "Scala client", "scala-client", "scala_client", "dsl-client-scala-http_2.10");
	}

	@Override
	public void build(final File sources, final Context context) throws ExitException {
		final File depsRoot = Dependencies.getDependenciesRoot(context);
		final File scalaDeps = new File(depsRoot.getAbsolutePath(), "scala_client");
		final String customJar = context.get("scala_client");
		final File model = new File(customJar != null ? customJar : "./generated-model-scala.jar");
		final Either<String> compilation = ScalaCompilation.compile("scala_client", scalaDeps, sources, model, context);
		if (!compilation.isSuccess()) {
			context.error("Error during Scala client library compilation.");
			context.error(compilation.whyNot());
			throw new ExitException();
		}
		if (model.exists()) {
			context.show("Compiled Scala client library to: " + model.getAbsolutePath());
		} else {
			context.error("Can't seem to find compiled Scala client library: " + model.getAbsolutePath());
			context.log(compilation.get());
			throw new ExitException();
		}
	}
}
