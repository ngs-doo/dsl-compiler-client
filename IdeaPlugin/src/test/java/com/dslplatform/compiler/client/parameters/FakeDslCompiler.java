package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.Context;
import com.dslplatform.compiler.client.Either;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In-memory stand-in for the DSL compiler server.
 * <p>
 * Unit tests must not start a real mono/.NET process, so this fake answers parse/findRule calls
 * from a configurable handler instead of talking to dsl-compiler.exe over a socket. It lives in
 * the compiler client package on purpose: TokenParser's constructor and the ParseResult/
 * SyntaxConcept/RuleInfo constructors are package-private, which keeps the fake free of reflection.
 */
public class FakeDslCompiler extends DslCompiler.TokenParser {

	public interface ParseHandler {
		Either<DslCompiler.ParseResult> parse(String dsl);
	}

	public interface RuleHandler {
		Either<DslCompiler.RuleInfo> findRule(String name);
	}

	public final AtomicInteger parseCount = new AtomicInteger();
	public final AtomicInteger findRuleCount = new AtomicInteger();
	public final List<String> parsedTexts = new CopyOnWriteArrayList<String>();
	public volatile ParseHandler handler;
	public volatile RuleHandler ruleHandler;

	public FakeDslCompiler() throws Exception {
		super(new SilentContext(), new File("fake-dsl-compiler"), 0, dummyProcess());
	}

	@Override
	public Either<DslCompiler.ParseResult> parse(String dsl) {
		parseCount.incrementAndGet();
		parsedTexts.add(dsl);
		final ParseHandler h = handler;
		return h != null ? h.parse(dsl) : Either.fail("FakeDslCompiler has no parse handler");
	}

	@Override
	public Either<DslCompiler.RuleInfo> findRule(String name) {
		findRuleCount.incrementAndGet();
		final RuleHandler h = ruleHandler;
		return h != null ? h.findRule(name) : Either.fail("Unknown rule: " + name);
	}

	public static DslCompiler.SyntaxConcept concept(DslCompiler.SyntaxType type, String value, int line, int column) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("Type", type.name());
		map.put("Value", value);
		map.put("Script", "");
		map.put("Line", line);
		map.put("Column", column);
		return new DslCompiler.SyntaxConcept(map);
	}

	public static Either<DslCompiler.ParseResult> tokens(DslCompiler.SyntaxConcept... concepts) {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>(concepts.length);
		for (DslCompiler.SyntaxConcept c : concepts) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("Type", c.type.name());
			map.put("Value", c.value);
			map.put("Script", c.script);
			map.put("Line", c.line);
			map.put("Column", c.column);
			list.add(map);
		}
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("Tokens", list);
		return Either.success(new DslCompiler.ParseResult(result));
	}

	public static DslCompiler.RuleInfo rule(String name, String grammar, String description) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("Rule", name);
		map.put("Grammar", grammar);
		map.put("Children", new ArrayList<Object>());
		map.put("Description", description);
		return new DslCompiler.RuleInfo(map);
	}

	private static Process dummyProcess() {
		try {
			String os = System.getProperty("os.name", "").toLowerCase();
			ProcessBuilder builder = os.contains("win")
					? new ProcessBuilder("cmd", "/c", "exit 0")
					: new ProcessBuilder("/bin/true");
			return builder.start();
		} catch (Exception e) {
			return null;   // the monitor threads tolerate a missing process
		}
	}

	private static class SilentContext extends Context {
		SilentContext() {
			super(new PrintStream(new OutputStream() {
				@Override
				public void write(int b) {
				}
			}));
		}
	}
}
