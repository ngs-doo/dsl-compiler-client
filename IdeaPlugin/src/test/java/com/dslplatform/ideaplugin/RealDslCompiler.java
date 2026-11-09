package com.dslplatform.ideaplugin;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.Context;
import com.dslplatform.compiler.client.Either;
import com.dslplatform.compiler.client.Main;
import com.dslplatform.compiler.client.parameters.Download;
import com.dslplatform.compiler.client.parameters.DslCompiler;
import com.intellij.openapi.diagnostic.Logger;

import java.io.File;
import java.util.Arrays;

/**
 * Boots the real DSL compiler for integration tests; skipped when mono/dsl-compiler.exe are unavailable.
 */
public final class RealDslCompiler {

	public static DslCompiler.TokenParser create() throws Exception {
		final Context context = new DslContext(Logger.getInstance("RealDslCompiler"));
		context.put(Download.INSTANCE, "");
		context.put(DslCompiler.INSTANCE, "");
		if (!Main.processContext(context, Arrays.<CompileParameter>asList(Download.INSTANCE, DslCompiler.INSTANCE))) {
			throw new IllegalStateException("Unable to setup DSL Platform client - is Mono installed?");
		}
		final File compiler = new File(context.get(DslCompiler.INSTANCE));
		final Either<DslCompiler.TokenParser> trySetup = DslCompiler.setupServer(context, compiler);
		if (!trySetup.isSuccess()) {
			throw new IllegalStateException("Unable to start DSL compiler: " + trySetup.explainError());
		}
		final DslCompiler.TokenParser parser = trySetup.get();
		for (int i = 0; i < 500; i++) {
			if (parser.parse("module test;").isSuccess()) {
				return parser;
			}
			Thread.sleep(100);
		}
		parser.close();
		throw new IllegalStateException("DSL compiler did not become ready within 50 seconds");
	}

	private RealDslCompiler() {
	}
}
