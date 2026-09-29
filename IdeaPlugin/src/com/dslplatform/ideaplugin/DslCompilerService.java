package com.dslplatform.ideaplugin;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.Either;
import com.dslplatform.compiler.client.Main;
import com.dslplatform.compiler.client.parameters.Download;
import com.dslplatform.compiler.client.parameters.DslCompiler;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAwareRunnable;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

@Service(Service.Level.APP)
public final class DslCompilerService {

	private DslCompiler.TokenParser tokenParser;
	private boolean parserVerified = false;
	private boolean rulesReady = false;
	private final Logger logger;

	public DslCompilerService() {
		logger = Logger.getInstance("DSL Platform");
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

	boolean isReady() {
		return parserVerified;
	}

	Either<DslCompiler.RuleInfo> findRule(String name) {
		final DslCompiler.TokenParser parser = tokenParser;
		if (parser == null || !rulesReady) return Either.fail("DSL rules not loaded yet");
		try {
			synchronized (this) {
				return parser.findRule(name);
			}
		} catch (Exception e) {
			return Either.fail(e.getMessage());
		}
	}

	boolean areRulesReady() {
		return rulesReady;
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
			logger.debug("DSL Platform compiler found at: " + compiler.getAbsolutePath());
			Either<DslCompiler.TokenParser> trySetup = DslCompiler.setupServer(context, compiler);
			if (trySetup.isSuccess()) {
				tokenParser = trySetup.get();
				ApplicationManager.getApplication().executeOnPooledThread(new DumbAwareRunnable() {
					@Override
					public void run() {
						warmUpParser();
					}
				});
				ApplicationManager.getApplication().executeOnPooledThread(new DumbAwareRunnable() {
					@Override
					public void run() {
						loadRulesInBackground();
					}
				});
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
			}
		}
	}

	public static class Analysis {
		public final List<AST> ast;
		public final List<RuleRegion> regions;

		Analysis(List<AST> ast, List<RuleRegion> regions) {
			this.ast = ast;
			this.regions = regions;
		}
	}

	public static class RuleRegion {
		public final int start;
		public final int end;
		public final String name;

		RuleRegion(int start, int end, String name) {
			this.start = start;
			this.end = end;
			this.name = name;
		}
	}

	Either<Analysis> analyzeFull(String dsl) {
		if (dsl.trim().isEmpty()) {
			return Either.success(new Analysis(new ArrayList<AST>(0), new ArrayList<RuleRegion>(0)));
		}
		if (tokenParser == null) return Either.fail("Token parser not ready");
		Either<List<DslCompiler.SyntaxConcept>> tryParsed = parseTokens(dsl);
		if (!tryParsed.isSuccess()) {
			return Either.fail(tryParsed.explainError());
		}
		List<DslCompiler.SyntaxConcept> parsed = tryParsed.get();
		String[] lines = dsl.split("\\n");
		int[] linesTotal = new int[lines.length];
		int runningTotal = 0;
		for (int i = 0; i < lines.length; i++) {
			linesTotal[i] = runningTotal;
			runningTotal += lines[i].length() + 1;
		}

		List<AST> newAst = new ArrayList<AST>(parsed.size() * 2);
		Stack<AST> stack = new Stack<AST>();
		AST current = null;
		for (DslCompiler.SyntaxConcept c : parsed) {
			int off = linesTotal[c.line - 1] + c.column;
			int len = c.value.length();
			switch (c.type) {
				case RuleStart:
					stack.push(current = new AST(c, off, len, current));
					break;
				case RuleEnd:
					if (!stack.isEmpty()) {
						stack.pop();
						current = stack.isEmpty() ? null : stack.peek();
					}
					break;
				case Keyword:
				case Identifier:
				case StringQuote:
					newAst.add(new AST(c, off, len, current));
					break;
			}
		}
		newAst.sort(AST.SORT);
		return Either.success(new Analysis(newAst, extractRegions(parsed, linesTotal, dsl.length())));
	}

	Either<List<AST>> analyze(String dsl) {
		Either<Analysis> full = analyzeFull(dsl);
		if (!full.isSuccess()) return Either.fail(full.explainError());
		return Either.success(full.get().ast);
	}

	private static List<RuleRegion> extractRegions(List<DslCompiler.SyntaxConcept> tokens, int[] linesTotal, int textLength) {
		List<RuleRegion> regions = new ArrayList<RuleRegion>();
		ArrayList<String> names = new ArrayList<String>();
		ArrayList<Integer> starts = new ArrayList<Integer>();
		ArrayList<Integer> levels = new ArrayList<Integer>();
		for (DslCompiler.SyntaxConcept t : tokens) {
			int off = linesTotal[t.line - 1] + t.column;
			if (t.type == DslCompiler.SyntaxType.RuleExtension) {
				names.add(t.value);
				starts.add(off);
				levels.add(0);
			} else if (t.type == DslCompiler.SyntaxType.RuleEnd && !levels.isEmpty()) {
				int idx = levels.size() - 1;
				int level = levels.get(idx) - 1;
				levels.set(idx, level);
				if (level >= 0) continue;
				regions.add(new RuleRegion(starts.get(idx), off, t.value));
				names.remove(idx);
				starts.remove(idx);
				levels.remove(idx);
				if (!levels.isEmpty()) {
					int parentIdx = levels.size() - 1;
					levels.set(parentIdx, levels.get(parentIdx) - 1);
				}
			} else if (t.type == DslCompiler.SyntaxType.RuleStart && !levels.isEmpty()) {
				int idx = levels.size() - 1;
				levels.set(idx, levels.get(idx) + 1);
			}
		}
		for (int i = names.size() - 1; i >= 0; i--) {
			regions.add(new RuleRegion(starts.get(i), textLength, names.get(i)));
		}
		return regions;
	}

	Either<List<RuleRegion>> computeRegions(String dsl) {
		Either<Analysis> full = analyzeFull(dsl);
		if (!full.isSuccess()) return Either.fail(full.explainError());
		return Either.success(full.get().regions);
	}

	void refreshRegions(final DslFile file, final String text) {
		if (!isReady()) {
			file.refreshSkipped(text);
			return;
		}
		Either<List<RuleRegion>> tryRegions = computeRegions(text);
		if (tryRegions.isSuccess()) {
			file.storeRegions(text, tryRegions.get());
		} else {
			file.refreshFailed(text);
			logger.debug("DSL region refresh failed (" + text.length() + " chars): " + tryRegions.explainError());
		}
	}

	private void loadRulesInBackground() {
		final long start = System.currentTimeMillis();
		int attempt = 0;
		while (!rulesReady && attempt < 1000) {
			final DslCompiler.TokenParser parser = tokenParser;
			if (parser == null) return;
			Either<DslCompiler.RuleInfo> tryRule;
			synchronized (this) {   // same monitor as parseTokens: no concurrent socket use
				try {
					tryRule = parser.findRule("");
				} catch (Exception e) {
					tryRule = Either.fail(e.getMessage());
				}
			}
			if (tryRule.isSuccess() || isRulesTableLoaded(tryRule)) {
				rulesReady = true;
				logger.warn("DSL Platform rules loaded in " + (System.currentTimeMillis() - start) + " ms (attempt " + attempt + ")");
				return;
			}
			attempt++;
			logger.debug("DSL Platform rules not loaded yet (attempt " + attempt + "): " + tryRule.explainError());
			try {
				Thread.sleep(Math.min(1000L * attempt, 10000L));
			} catch (InterruptedException e) {
				return;
			}
		}
	}

	private static boolean isRulesTableLoaded(Either<DslCompiler.RuleInfo> tryRule) {
		final String error = tryRule.explainError();
		return error != null && error.startsWith("Unknown rule");
	}

	void warmUpParser() {
		long start = System.currentTimeMillis();
		logger.debug("DSL Platform parser warming up - the first parse makes the server build its rule set, this can take a while");
		for (int attempt = 1; ; attempt++) {
			if (tokenParser == null) return;
			Either<List<DslCompiler.SyntaxConcept>> result = null;
			String error = null;
			try {
				result = parseTokens("module warmup {\n}\n");
			} catch (Exception e) {
				error = e.getMessage();
			}
			if (result != null && result.isSuccess()) {
				logger.debug("DSL Platform parser warmed up in " + (System.currentTimeMillis() - start) + " ms (attempt " + attempt + ")");
				parserVerified = true;
				return;
			}
			if (System.currentTimeMillis() - start > 5 * 60 * 1000L) {
				logger.warn("DSL Platform parser warmup gave up after " + (System.currentTimeMillis() - start) + " ms: "
					+ (error != null ? error : (result == null ? "unknown" : result.explainError())));
				parserVerified = true;
				return;
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}
		}
	}

	private synchronized Either<List<DslCompiler.SyntaxConcept>> parseTokens(String dsl) {
		Either<DslCompiler.ParseResult> result = tokenParser.parse(dsl);
		if (!result.isSuccess() || result.get().tokens == null) {
			return Either.fail("Parser not ready");
		}
		return Either.success(result.get().tokens);
	}

}
