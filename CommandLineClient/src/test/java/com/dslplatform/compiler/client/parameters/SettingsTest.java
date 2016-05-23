package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.Context;
import com.dslplatform.compiler.client.ContextMock;
import com.dslplatform.compiler.client.Main;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class SettingsTest {

	@Test
	public void canParseUnknownSettingsWithForce() throws IOException {
		ContextMock ctx = new ContextMock();
		ctx.put(Settings.INSTANCE, "manual-json,unknown");
		ctx.put(Force.INSTANCE, null);
		boolean result = Main.processContext(ctx, Collections.<CompileParameter>singletonList(Settings.INSTANCE));
		assertTrue(result);
		List<String> settings = Settings.get(ctx);
		assertEquals(2, settings.size());
		assertEquals("unknown", settings.get(1));
		assertFalse(ctx.hasError);
		assertTrue(ctx.hasWarning);
		assertTrue(ctx.warning.contains("Unknown setting: unknown"));
	}

	@Test
	public void cantParseUnknownSettingsWithoutForce() throws IOException {
		ContextMock ctx = new ContextMock();
		ctx.put(Settings.INSTANCE, "manual-json,unknown");
		boolean result = Main.processContext(ctx, Collections.<CompileParameter>singletonList(Settings.INSTANCE));
		assertFalse(result);
		assertNull(Settings.get(ctx));
		assertTrue(ctx.hasError);
		assertFalse(ctx.hasWarning);
		assertTrue(ctx.error.contains("Unknown setting: unknown"));
	}

	@Test
	public void sourceOnlyDetectedInMultiSettings() throws IOException {
		ContextMock ctx = new ContextMock();
		ctx.put(Settings.INSTANCE, "manual-json,source-only");
		boolean result = Main.processContext(ctx, Collections.<CompileParameter>singletonList(Settings.INSTANCE));
		assertTrue(result);
		assertTrue(Settings.hasSourceOnly(ctx));
		assertEquals(2, Settings.get(ctx).size());
		assertFalse(ctx.hasError);
		assertFalse(ctx.hasWarning);
	}
}
