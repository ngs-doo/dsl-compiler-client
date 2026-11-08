package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.Context;
import com.dslplatform.compiler.client.Utils;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeFalse;

public class MonoTest {

	@Test
	public void validMonoPathPassesCheck() throws IOException {
		assumeFalse(Utils.isWindows());
		final File fakeMono = File.createTempFile("fake-mono", ".sh");
		fakeMono.deleteOnExit();
		final FileWriter writer = new FileWriter(fakeMono);
		writer.write("#!/bin/sh\necho 'Mono JIT Compiler version 6.12.0'\n");
		writer.close();
		fakeMono.setExecutable(true);

		final Context context = new Context();
		context.put(Mono.INSTANCE, fakeMono.getAbsolutePath());
		assertTrue(Mono.INSTANCE.check(context));
	}

	@Test
	public void invalidMonoPathFailsCheck() {
		final Context context = new Context();
		context.put(Mono.INSTANCE, "/no/such/mono/path");
		assertFalse(Mono.INSTANCE.check(context));
	}
}
