package com.dslplatform.compiler.client;

import java.io.*;
import java.net.URL;

public abstract class DslServer {
	private static final String REMOTE_URL = "https://compiler.dsl-platform.com:8443/platform/download/";

	public static void downloadAndUnpack(final Context context, final String file, final File path) throws IOException {
		final URL server = new URL(REMOTE_URL + file + ".zip");
		context.log("Downloading " + file + ".zip ...");
		Utils.unpackZip(context, path, server);
	}
}
