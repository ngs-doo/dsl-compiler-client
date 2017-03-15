package com.dslplatform.compiler.client;

import com.dslplatform.compiler.client.parameters.Download;
import com.dslplatform.compiler.client.parameters.DslCompiler;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.*;

public class TokensTest {

	@Test
	public void testTokensParsing() throws IOException, InterruptedException {
		ContextMock context = new ContextMock();
		assertTrue(Main.processContext(context, Arrays.<CompileParameter>asList(Download.INSTANCE, DslCompiler.INSTANCE)));
		String path = context.get(DslCompiler.INSTANCE);
		assertNotNull(path);
		File compiler = new File(path);
		assertTrue(compiler.exists());
		Either<DslCompiler.TokenParser> trySetup = DslCompiler.setupServer(context, compiler);
		assertTrue(trySetup.isSuccess());
		int i = 0;
		for(;i < 500; i++) {
			Either<DslCompiler.ParseResult> module = trySetup.get().parse("module test;");
			if (!module.isSuccess()) {
				Thread.sleep(100);
				continue;
			}
			assertNull(module.get().error);
			assertTrue(module.get().tokens.size() > 0);
			i = 505;
			break;
		}
		assertEquals(505, i);
		trySetup.get().close();
	}

	@Test
	public void testRules() throws IOException, InterruptedException {
		ContextMock context = new ContextMock();
		assertTrue(Main.processContext(context, Arrays.<CompileParameter>asList(Download.INSTANCE, DslCompiler.INSTANCE)));
		String path = context.get(DslCompiler.INSTANCE);
		assertNotNull(path);
		File compiler = new File(path);
		assertTrue(compiler.exists());
		Either<DslCompiler.TokenParser> trySetup = DslCompiler.setupServer(context, compiler);
		assertTrue(trySetup.isSuccess());
		int i = 0;
		for(;i < 500; i++) {
			Either<DslCompiler.RuleInfo> rule = trySetup.get().findRule("module_rule");
			if (!rule.isSuccess()) {
				Thread.sleep(100);
				continue;
			}
			assertEquals("module_rule", rule.get().rule);
			i = 505;
			break;
		}
		assertEquals(505, i);
		trySetup.get().close();
	}
}
