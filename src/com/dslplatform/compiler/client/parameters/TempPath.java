package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.Context;
import com.dslplatform.compiler.client.InputParameter;
import com.dslplatform.compiler.client.Utils;

import java.io.File;
import java.io.IOException;
import java.util.*;

public enum TempPath implements CompileParameter {
	INSTANCE;

	private static final String CACHE_NAME = "temp_path_cache";

	public static File getTempPath(final Context context) {
		return context.load(CACHE_NAME);
	}

	private static boolean prepareSystemTempPath(final Context context) {
		try {
			final String projectLocation = System.getProperty("user.dir");
			final File parentFolder = new File(projectLocation).getParentFile();
			if (parentFolder == null) {
				context.error("Unable to detect parent folder. Current path: " + projectLocation);
				return false;
			}
			final char pathSeparator = Utils.isWindows() ? '\\' : '/';
			final String projectName = projectLocation.lastIndexOf(pathSeparator) < projectLocation.length() - 1
					? projectLocation.substring(projectLocation.lastIndexOf(pathSeparator) + 1)
					: projectLocation.substring(projectLocation.substring(0, projectLocation.length() - 2).lastIndexOf(pathSeparator) + 1, projectLocation.length() - 1);
			final String rnd = UUID.randomUUID().toString();
			final File temp = File.createTempFile(rnd, ".dsl-test");
			final File path = new File(temp.getParentFile().getAbsolutePath() + "/DSL-Platform/" + projectName);
			if (!temp.delete()) {
				context.error("Unable to remove temporary created file: " + temp.getAbsolutePath());
				return false;
			}
			if (path.exists()) {
				Utils.deletePath(path);
			} else if (!path.mkdir()) {
				context.error("Error creating temporary path in: " + path.getAbsolutePath());
				return false;
			}
			context.cache(CACHE_NAME, path);
			return true;
		} catch (IOException e) {
			context.error("Error preparing system temporary path.");
			context.error(e);
			return false;
		}
	}

	@Override
	public boolean check(final Context context) {
		if (context.contains(InputParameter.TEMP)) {
			final String value = context.get(InputParameter.TEMP);
			if (value != null && value.length() > 0) {
				final File path = new File(value);
				if (!path.exists()) {
					context.error("Temporary path provided (" + value + "), but doesn't exists. Please create it or use system path.");
					return false;
				}
				if (!path.isDirectory()) {
					context.error("Temporary path provided, but it's not a directory: " + value);
					return false;
				}
				context.cache(CACHE_NAME, path);
				return true;
			}
		}
		return prepareSystemTempPath(context);
	}

	@Override
	public void run(final Context context) {
	}

	@Override
	public String getShortDescription() {
		return "Use custom temporary path instead of system default";
	}

	@Override
	public String getDetailedDescription() {
		return "Files downloaded from DSL Platform will be stored to temporary path.\n" +
				"When unspecified /DSL-Platform folder in system default temporary path will be used.";
	}
}
