package com.dslplatform.compiler.client;

import com.dslplatform.compiler.client.parameters.ApplyMigration;
import com.dslplatform.compiler.client.parameters.DbConnection;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MigrationTest {

	static class ContextMock extends Context {
		public String message;
		public boolean hasLog;
		public boolean hasError;

		@Override public void show(final String... values) { message = values[0]; }
		@Override public void log(final String value) { hasLog = true; }
		@Override public void log(final char[] value, final int len) { hasLog = true; }
		@Override public void error(final String value) { hasError = true; }
		@Override public void error(final Exception ex) { hasError = true; }
		@Override public boolean canInteract() { return false; }
	}

	static class MockMigration implements CompileParameter {

		@Override public String getAlias() { return "migration"; }
		@Override public String getUsage() { return null; }
		@Override public String getShortDescription() { return null; }
		@Override public String getDetailedDescription() { return null; }
		@Override public boolean check(Context context) throws ExitException { return true; }

		public boolean inContext;
		private File file;

		public MockMigration(File file) {
			this.file = file;
		}

		@Override
		public void run(Context context) throws ExitException {
			inContext = context.contains(this);
			context.cache("migration_file", file);
		}
	}

	@Test
	public void testApplyWithoutMigration() throws IOException {
		File tmp = new File("dummy-migration-" + UUID.randomUUID() + ".sql");
		Utils.saveFile(tmp, "");
		ContextMock ctx = new ContextMock();
		MockMigration mm = new MockMigration(tmp);
		ctx.put(ApplyMigration.INSTANCE, null);
		ctx.put(DbConnection.INSTANCE, "dummy connection string");
		assertFalse(mm.inContext);
		Main.processContext(ctx, Arrays.asList(mm, ApplyMigration.INSTANCE));
		assertTrue(tmp.delete());
		assertTrue(mm.inContext);
		assertFalse(ctx.hasError);
		assertFalse(ctx.hasLog);
		assertEquals("Nothing to apply.", ctx.message);
	}
}
