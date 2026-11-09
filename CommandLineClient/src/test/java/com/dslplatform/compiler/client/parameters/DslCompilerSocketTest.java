package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.Context;
import com.dslplatform.compiler.client.Either;
import org.junit.Test;

import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class DslCompilerSocketTest {

	@Test
	public void truncatedResponseIsReported() throws Exception {
		final ServerSocket server = new ServerSocket(0);
		final Thread responder = new Thread(new Runnable() {
			@Override
			public void run() {
				try (final Socket socket = server.accept()) {
					socket.getInputStream().read(new byte[4096]);
					final OutputStream out = socket.getOutputStream();
					// 4-byte status word + 4-byte big-endian length, claiming 100 bytes
					out.write(new byte[]{(byte) 'O', 0, 0, 0, 0, 0, 0, 100});
					out.flush();
					Thread.sleep(300);
					out.write("partial".getBytes(StandardCharsets.UTF_8)); // only 7 of the claimed 100 bytes
					out.flush();
				} catch (final Exception ignore) {
				}
			}
		});
		responder.setDaemon(true);
		responder.start();

		// runCompilerSocket writes its own command - the responder's single read() must see exactly that one write, or leftover bytes make the server close with RST instead of EOF
		final Socket client = new Socket("127.0.0.1", server.getLocalPort());
		try {
			final Method method = DslCompiler.class.getDeclaredMethod("runCompilerSocket", Context.class, Socket.class, List.class);
			method.setAccessible(true);
			@SuppressWarnings("unchecked")
			final Either<byte[]> result = (Either<byte[]>) method.invoke(null, new Context(), client, Arrays.asList("target=java_client"));
			assertFalse(result.isSuccess());
			assertTrue(result.whyNot().getMessage().contains("Missing"));
		} finally {
			client.close();
			server.close();
		}
	}
}
