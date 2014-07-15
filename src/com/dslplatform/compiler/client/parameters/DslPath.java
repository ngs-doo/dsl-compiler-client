package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.CompileParameter;
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

	private static Map<String, String> cache;

	public static Map<String, String> getCurrentDsl(final Map<InputParameter, String> parameters) {
		if (cache != null) {
			return cache;
		}
		String value = parameters.get(InputParameter.DSL);
		if (value == null) {
			if (!(new File("./dsl").exists())) {
				System.out.println("DSL path not provided. Can't use default path (./dsl) since it doesn't exists");
				System.exit(0);
			}
			parameters.put(InputParameter.DSL, value = "./dsl");
		}
		final File dslPath = new File(value).getAbsoluteFile();
		final List<File> dslFiles = Utils.findDslFiles(dslPath);
		final Map<String, String> dslMap = new LinkedHashMap<String, String>();
		final int pathLen = dslPath.getAbsolutePath().length();
		for (final File file : dslFiles) {
			if (!file.canRead()) {
				System.out.println("Can't read DSL file: " + file.getName());
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
				System.out.println("Error reading DSL file: " + file.getName());
				System.exit(0);
			}
		}
		return cache = dslMap;
	}

	@Override
	public boolean check(final Map<InputParameter, String> parameters) {
		final String value = parameters.get(InputParameter.DSL);
		if (value == null) {
			final File dslPath = new File("./dsl");
			if (!dslPath.exists()) {
				System.out.println("DSL path not provided. Can't use default path (./dsl) since it doesn't exists");
				return false;
			}
			parameters.put(InputParameter.DSL, "./dsl");
		} else {
			final File dslPath = new File(value);
			if (!dslPath.exists()) {
				System.out.println("Provided DSL path (" + value + ") does not exists. Please provide valid path to DSL files");
				return false;
			}
		}
		return true;
	}

	@Override
	public void run(final Map<InputParameter, String> parameters) {
	}

	@Override
	public String getShortDescription() {
		return "path to DSL files";
	}

	@Override
	public String getDetailedDescription() {
		return null;
	}
}
