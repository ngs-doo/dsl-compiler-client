package com.dslplatform.compiler.client.parameters.build;

import com.dslplatform.compiler.client.*;
import com.dslplatform.compiler.client.parameters.Dependencies;
import com.dslplatform.compiler.client.parameters.DotNet;
import com.dslplatform.compiler.client.parameters.Download;
import com.dslplatform.compiler.client.parameters.Maven;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;

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
		final Either<String> compilation = ScalaCompilation.compile(scalaDeps, sources, model, context);
		if (!compilation.isSuccess()) {
			context.error("Error during Scala client library compilation.");
			context.error(compilation.whyNot());
			throw new ExitException();
		}
		if (model.exists()) {
			context.show("Compiled Scala client library to: " + model.getAbsolutePath());
		} else {
			context.error("Can't seem to find compiled Scala client library: " + model.getAbsolutePath());
			context.show(compilation.get());
			throw new ExitException();
		}
	}
}
