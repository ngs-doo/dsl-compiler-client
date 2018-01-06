package com.dslplatform.ideaplugin;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.Either;
import com.dslplatform.compiler.client.Main;
import com.dslplatform.compiler.client.parameters.Download;
import com.dslplatform.compiler.client.parameters.DslCompiler;
import com.intellij.openapi.diagnostic.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public class DslCompilerService {

	private DslCompiler.TokenParser tokenParser;
	private final List<Runnable> notifications = new ArrayList<Runnable>();

	public DslCompilerService() {
		final Logger logger = com.intellij.openapi.diagnostic.Logger.getInstance("DSL Platform");
		final DslContext context = new DslContext(logger);
		context.put(Download.INSTANCE, null);
		Thread setup = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					setupCompiler(logger, context);
				} catch (Throwable e) {
					logger.error(e.getMessage());
				}
			}
		});
		setup.start();
	}

	boolean callWhenReady(Runnable callback) {
		if (tokenParser == null) {
			notifications.add(callback);
			if (tokenParser != null) {
				callback.run();
				return false;
			}
			return true;
		}
		return false;
	}

	private void setupCompiler(Logger logger, DslContext context) throws InterruptedException {
		if (!Main.processContext(context, Arrays.<CompileParameter>asList(Download.INSTANCE, DslCompiler.INSTANCE))) {
			logger.warn("Unable to setup DSL Platform client");
		}
		final String path = context.get(DslCompiler.INSTANCE);
		if (path == null) {
			logger.error("Unable to setup dsl-compiler.exe. Please check if Mono/.NET is installed and available on path.");
		} else {
			final File compiler = new File(path);
			logger.info("DSL Platform compiler found at: " + compiler.getAbsolutePath());
			Either<DslCompiler.TokenParser> trySetup = DslCompiler.setupServer(context, compiler);
			if (trySetup.isSuccess()) {
				tokenParser = trySetup.get();
				Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							tokenParser.close();
							tokenParser = null;
						} catch (Exception ignore) {
						}
					}
				}));
				Thread.sleep(2000);
				for (Runnable r : notifications) {
					r.run();
				}
			}
		}
	}

	Either<List<AST>> analyze(String dsl) {
		if (dsl.trim().isEmpty()) {
			List<AST> empty = new ArrayList<AST>(0);
			return Either.success(empty);
		}
		if (tokenParser == null) return Either.fail("Token parser not ready");
		Either<List<DslCompiler.SyntaxConcept>> tryParsed = parseTokens(dsl);
		if (!tryParsed.isSuccess()) {
			return Either.fail(tryParsed.explainError());
		}
		List<DslCompiler.SyntaxConcept> parsed = tryParsed.get();
		List<AST> newAst = new ArrayList<AST>(parsed.size() * 2);
		String[] lines = dsl.split("\\n");
		int[] linesTotal = new int[lines.length];
		int runningTotal = 0;
		for (int i = 0; i < lines.length; i++) {
			linesTotal[i] = runningTotal;
			runningTotal += lines[i].length() + 1;
		}
		Stack<AST> stack = new Stack<AST>();
		AST current = null;
		for (DslCompiler.SyntaxConcept c : parsed) {
			int off = linesTotal[c.line - 1] + c.column;
			int len = c.value.length();
			switch (c.type) {
				case RuleStart:
					stack.push(current = new AST(c, off, len, current));
					//newAst.add(current);
					break;
				case RuleEnd:
					stack.pop();
					current = stack.isEmpty() ? null : stack.peek();
					break;
				case Keyword:
				case Identifier:
				case StringQuote:
					newAst.add(new AST(c, off, len, current));
					break;
			}
		}
		return Either.success(newAst);
	}

	private synchronized Either<List<DslCompiler.SyntaxConcept>> parseTokens(String dsl) {
		Either<DslCompiler.ParseResult> result = tokenParser.parse(dsl);
		if (!result.isSuccess() || result.get().tokens == null) {
			return Either.fail("Parser not ready");
		}
		return Either.success(result.get().tokens);
	}

}
