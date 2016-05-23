package com.dslplatform.compiler.client;

import com.dslplatform.compiler.client.parameters.ApplyMigration;
import com.dslplatform.compiler.client.parameters.PostgresConnection;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.Assert.*;

public class MigrationTest {

	static class MockMigration implements CompileParameter {

		@Override
		public String getAlias() {
			return "migration";
		}

		@Override
		public String getUsage() {
			return null;
		}

		@Override
		public String getShortDescription() {
			return null;
		}

		@Override
		public String getDetailedDescription() {
			return null;
		}

		@Override
		public boolean check(Context context) throws ExitException {
			return true;
		}

		public boolean inContext;
		private File file;

		public MockMigration(File file) {
			this.file = file;
		}

		@Override
		public void run(Context context) throws ExitException {
			inContext = context.contains(this);
			context.cache("postgres_migration_file", file);
		}
	}

	@Test
	public void testApplyWithoutMigration() throws IOException {
		File tmp = new File("dummy-migration-" + UUID.randomUUID() + ".sql");
		Utils.saveFile(new ContextMock(), tmp, "");
		ContextMock ctx = new ContextMock();
		MockMigration mm = new MockMigration(tmp);
		ctx.put(ApplyMigration.INSTANCE, null);
		ctx.put(PostgresConnection.INSTANCE, "dummy connection string");
		assertFalse(mm.inContext);
		Main.processContext(ctx, Arrays.asList(mm, ApplyMigration.INSTANCE));
		assertTrue(tmp.delete());
		assertTrue(mm.inContext);
		assertFalse(ctx.hasError);
		assertFalse(ctx.hasLog);
		assertEquals("Nothing to apply.", ctx.message);
	}
}
