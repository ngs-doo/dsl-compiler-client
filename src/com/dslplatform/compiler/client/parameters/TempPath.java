package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.InputParameter;

import java.io.File;
import java.io.IOException;
import java.util.*;

public enum TempPath implements CompileParameter {
	INSTANCE;

	private static File cache;

	public static File getTempPath() {
		return cache;
	}

	private static boolean deletePath(final File path) {
		for (final String fn : path.list()) {
			final File f = new File(path, fn);
			if (f.isDirectory()) {
				if (!deletePath(f)) {
					System.out.println("Error cleaning up temporary directory. Failed to delete: " + f.getAbsolutePath());
					return false;
				}
			}
			if (!f.delete()) {
				System.out.println("Error cleaning up temporary file. Failed to delete: " + f.getAbsolutePath());
				return false;
			}
		}
		return true;
	}

	private static boolean prepareSystemTempPath() {
		try {
			final String projectLocation = TempPath.class.getProtectionDomain().getCodeSource().getLocation().getPath();
			final String projectName = projectLocation.lastIndexOf('/') < projectLocation.length() - 1
					? projectLocation.substring(projectLocation.lastIndexOf('/') + 1)
					: projectLocation.substring(projectLocation.substring(0, projectLocation.length() - 2).lastIndexOf('/') + 1, projectLocation.length() - 1);
			final String rnd = UUID.randomUUID().toString();
			final File temp = File.createTempFile(rnd, ".dsl-test");
			final File path = new File(temp.getParentFile().getAbsolutePath() + "/DSL-Platform/" + projectName);
			if (!temp.delete()) {
				System.out.println("Unable to remove temporary created file: " + temp.getAbsolutePath());
				return false;
			}
			if (path.exists()) {
				if(!deletePath(path)) {
					return false;
				}
			} else if (!path.mkdir()) {
				System.out.println("Error creating temporary path in: " + path.getAbsolutePath());
				return false;
			}
			cache = path;
			return true;
		} catch (IOException e) {
			System.out.println("Error preparing system temporary path");
			System.out.println(e.getMessage());
			return false;
		}
	}

	@Override
	public boolean check(final Map<InputParameter, String> parameters) {
		if (parameters.containsKey(InputParameter.TEMP)) {
			final String value = parameters.get(InputParameter.TEMP);
			if (value != null && value.length() > 0) {
				final File path = new File(value);
				if (!path.exists()) {
					System.out.println("Temporary path provided (" + value + "), but doesn't exists. Please create it or use system path.");
					return false;
				}
				if (!path.isDirectory()) {
					System.out.println("Temporary path provided, but it's not a directory: " + value);
					return false;
				}
				cache = path;
				return true;
			}
		}
		return prepareSystemTempPath();
	}

	@Override
	public void run(final Map<InputParameter, String> parameters) {
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
