package com.dslplatform.mojo;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.Context;
import com.dslplatform.compiler.client.Either;
import com.dslplatform.compiler.client.Main;
import com.dslplatform.compiler.client.parameters.*;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

abstract class Utils {

	static String resourceAbsolutePath(String resource) {
		if (resource == null) return null;

		try {
			String prefix = resource.startsWith("/") ? "" : "/";
			URL resourceUrl = Utils.class.getResource(prefix + resource);
			if (resourceUrl != null) {
				return new File(resourceUrl.toURI()).getAbsolutePath();
			}
		} catch (Exception ignore) {
		}

		File result = new File(resource);
		if (result.exists()) return result.getAbsolutePath();

		return null;
	}

	static String createDirIfNotExists(String dir) throws MojoExecutionException {
		if (dir == null || dir.length() == 0) return null;
		File file = new File(dir);
		if (!file.exists()) {
			if (!file.mkdirs()) {
				throw new MojoExecutionException("Error creating the dirs: " + file.getAbsolutePath());
			}
		} else if (!file.isDirectory()) {
			throw new MojoExecutionException("Expecting directory, buf found file at: " + file.getAbsolutePath());
		}
		return file.getAbsolutePath();
	}

	static Targets.Option targetOptionFrom(String value) {
		for (Targets.Option option : Targets.Option.values()) {
			if (option.toString().equals(value)) return option;
		}
		return null;
	}

	static String parseSettings(String[] value, Log log) throws MojoExecutionException {
		if (value == null || value.length == 0) return null;
		StringBuilder sb = new StringBuilder();
		for (String setting : value) {
			Settings.Option option = settingsOptionFrom(setting);
			if (option == null) {
				if (setting == null || setting.length() == 0 || setting.contains(" ")) {
					throw new MojoExecutionException("Invalid option passed as argument: " + setting);
				}
				if (log.isWarnEnabled()) {
					log.warn("Unrecognizable option: " + setting + ". Will try to pass it anyway.");
				}
			}
			sb.append(setting);
			sb.append(",");
		}
		return sb.length() > 0 ? sb.substring(0, sb.length() - 1) : null;
	}

	private static Settings.Option settingsOptionFrom(String value) {
		for (Settings.Option option : Settings.Option.values()) {
			if (option.toString().equals(value)) return option;
		}
		return null;
	}

	static void copyFolder(final File sources, final File target, final Context context) throws MojoExecutionException {
		for (final String fn : sources.list()) {
			final File sf = new File(sources, fn);
			final File tf = new File(target, fn);
			if (sf.isDirectory()) {
				if (!tf.mkdirs() && !tf.exists()) {
					String msg = "Failed to create target folder: " + tf.getAbsolutePath();
					context.error(msg);
					throw new MojoExecutionException(msg);
				}
				copyFolder(sf, tf, context);
			} else {
				final Either<String> content = com.dslplatform.compiler.client.Utils.readFile(sf);
				if (!content.isSuccess()) {
					String msg = "Error reading source file: " + sf.getAbsolutePath();
					context.error(msg);
					throw new MojoExecutionException(msg);
				}
				writeToFile(context, tf, content.get());
			}
		}
	}

	static void writeToFile(Context context, File file, String contents) throws MojoExecutionException {
		try {
			com.dslplatform.compiler.client.Utils.saveFile(context, file, contents);
		} catch (IOException e) {
			throw new MojoExecutionException("Error writing to file: " + file.getAbsolutePath() + ", contents: " + contents, e);
		}
	}

	static void appendToFile(Context context, File file, String contents) throws MojoExecutionException {
		if (!file.exists()) {
			writeToFile(context, file, contents);
		} else {
			try {
				FileWriter fw = new FileWriter(file, true);
				fw.append(contents);
				fw.close();
			} catch (IOException e) {
				throw new MojoExecutionException("Error appending to file: " + file.getAbsolutePath() + ", contents: " + contents, e);
			}
		}
	}

	static void runCompiler(MojoContext context, String plugins, String dsl, String compiler) throws MojoExecutionException {
		if (dsl != null && dsl.length() > 0) {
			File file = new File(dsl);
			if (!file.exists()) {
				throw new MojoExecutionException("DSL path specified, but path not found: " + dsl);
			}
			context.put(DslPath.INSTANCE, file.getAbsolutePath());
		}

		if (compiler != null && compiler.length() > 0) {
			File file = new File(compiler);
			if (!file.exists()) {
				throw new MojoExecutionException("Compiler path specified, but path not found: " + compiler);
			} else if (file.isDirectory()) {
				throw new MojoExecutionException("Please specify path to exe file, not directory. Detected path to directory: " + compiler);
			}
			context.put(DslCompiler.INSTANCE, file.getAbsolutePath());
		}
		if (plugins == null || plugins.length() == 0) {
			plugins = ".";
		}
		File pluginsFile = new File(plugins);
		if (!pluginsFile.exists()) {
			throw new MojoExecutionException("Specified plugins path not found: " + pluginsFile.getAbsolutePath());
		} else if (!pluginsFile.isDirectory()) {
			throw new MojoExecutionException("Please specify path to directory, not a specific file: " + pluginsFile.getAbsolutePath());
		}

		context.with(Force.INSTANCE).with(Download.INSTANCE).with(DisablePrompt.INSTANCE);

		List<CompileParameter> params = Main.initializeParameters(context, pluginsFile.getAbsolutePath());

		if (!Main.processContext(context, params)) {
			throw new MojoExecutionException(context.errorLog.toString());
		}
	}
}
