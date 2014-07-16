package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.Context;
import com.dslplatform.compiler.client.InputParameter;
import com.dslplatform.compiler.client.Utils;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public enum DslPath implements CompileParameter {
	INSTANCE;

	private static final String CACHE_NAME = "current_dsl_cache";

	public static Map<String, String> getCurrentDsl(final Context context) {
		final Map<String, String> cache = context.load(CACHE_NAME);
		if (cache != null) {
			return cache;
		}
		String value = context.get(InputParameter.DSL);
		if (value == null) {
			if (!(new File("./dsl").exists())) {
				context.error("DSL path not provided. Can't use default path (./dsl) since it doesn't exists");
				System.exit(0);
			}
			context.put(InputParameter.DSL, value = "./dsl");
		}
		final File dslPath = new File(value).getAbsoluteFile();
		final List<File> dslFiles = Utils.findDslFiles(dslPath);
		final Map<String, String> dslMap = new LinkedHashMap<String, String>();
		final int pathLen = dslPath.getAbsolutePath().length();
		for (final File file : dslFiles) {
			if (!file.canRead()) {
				context.error("Can't read DSL file: " + file.getName());
				System.exit(0);
			}
			try {
				final byte[] bytes = new byte[(int) file.length()];
				DataInputStream dis = new DataInputStream(new FileInputStream(file));
				dis.readFully(bytes);
				dis.close();
				final String relativeName = file.getAbsolutePath().substring(pathLen);
				dslMap.put(relativeName, new String(bytes, "UTF-8"));
			} catch (Exception ex) {
				context.error("Error reading DSL file: " + file.getName());
				context.error(ex);
				System.exit(0);
			}
		}
		context.cache(CACHE_NAME, dslMap);
		return dslMap;
	}

	@Override
	public boolean check(final Context context) {
		final String value = context.get(InputParameter.DSL);
		if (value == null) {
			final File dslPath = new File("./dsl");
			if (!dslPath.exists()) {
				context.error("DSL path not provided. Can't use default path (./dsl) since it doesn't exists");
				return false;
			}
			context.put(InputParameter.DSL, "./dsl");
		} else {
			final File dslPath = new File(value);
			if (!dslPath.exists()) {
				context.error("Provided DSL path (" + value + ") does not exists. Please provide valid path to DSL files");
				return false;
			}
		}
		return true;
	}

	@Override
	public void run(final Context context) {
	}

	@Override
	public String getShortDescription() {
		return "path to DSL files";
	}

	@Override
	public String getDetailedDescription() {
		return "Domain Specification Language files contain description of domain model in a language leveraging DDD (Domain-Driven-Design) concepts.\n" +
				"DSL files should contain descriptions of data structures used through the application and from which various parts of the application will be maintained.\n" +
				"Snapshot of DSL files will be saved to the database, for future comparison on database migrations.\n" +
				"\n" +
				"UTF-8 will be assumed for DSL files.\n" +
				".dsl and .ddd extensions are supported.";
	}
}
