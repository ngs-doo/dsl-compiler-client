package com.dslplatform.mojo.utils;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.Context;
import com.dslplatform.compiler.client.Either;
import com.dslplatform.compiler.client.parameters.*;
import com.google.common.primitives.Chars;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;

public class Utils {

	public static final CompileParameter[] ALL_COMPILE_PARAMETERS = {
			//Help.INSTANCE, // TODO: See what to do with this one
			//PropertiesFile.INSTANCE, // TODO: See what to do with this one
			GrantRole.INSTANCE,
			DslCompiler.INSTANCE,
			Version.INSTANCE,
			Diff.INSTANCE,
			Settings.INSTANCE,
			PostgresConnection.INSTANCE,
			TempPath.INSTANCE,
			Targets.INSTANCE,
			Migration.INSTANCE,
			VarraySize.INSTANCE,
			JavaPath.INSTANCE,
			DisableColors.INSTANCE,
			ApplyMigration.INSTANCE,
			DslPath.INSTANCE,
			LogOutput.INSTANCE,
			Namespace.INSTANCE,
			Mono.INSTANCE,
			OracleConnection.INSTANCE,
			Prompt.INSTANCE,
			Dependencies.INSTANCE,
			Parse.INSTANCE,
			Download.INSTANCE,
			Force.INSTANCE,
			Maven.INSTANCE,
			SqlPath.INSTANCE,
			DotNet.INSTANCE,
			ScalaPath.INSTANCE
	};

	/**
	 * @deprecated This is a bad idea, we should just use direct paths
	 */
	@Deprecated
	public static String resourceAbsolutePath(String resource) {
		if (resource == null) return null;

		try {
			String prefix = resource.startsWith("/") ? "" : "/";
			URL resourceUrl = Utils.class.getResource(prefix + resource);
			if (resourceUrl != null) {
				return new File(resourceUrl.toURI()).getAbsolutePath();
			}
		} catch (Exception e) {
		}

		File result = new File(resource);
		if (result.exists()) return result.getAbsolutePath();

		return null;
	}

	public static String createDirIfNotExists(String dir) throws MojoExecutionException {
		if (dir == null) return null;
		File file = new File(dir);
		if (!file.exists()) {
			if(!file.mkdirs()) {
				throw new MojoExecutionException("Error creating the dirs: " + file.getAbsolutePath());
			}
		}
		return file.getAbsolutePath();
	}

	public static Targets.Option targetOptionFrom(String value) {
		for (Targets.Option option : Targets.Option.values()) {
			if (nulleq(option.toString(), value)) return option;
		}
		return null;
	}

	public static Settings.Option settingsOptionFrom(String value) {
		for (Settings.Option option : Settings.Option.values()) {
			if (nulleq(option.toString(), value)) return option;
		}
		return null;
	}

	public static CompileParameter compileParameterFrom(String value) {
		for (CompileParameter compileParameter : ALL_COMPILE_PARAMETERS) {
			if (nulleq(compileParameter.getAlias(), value)) return compileParameter;
		}
		return null;
	}

	public static <T> boolean nulleq(T left, T right) {
		if (left == null && right == null) return true;
		if (left != null) return left.equals(right);
		if (right != null) return right.equals(left);
		return false;
	}

	public static String deNullify(String string) {
		return string == null ? "" : string;
	}

	public static void sanitizeDirectories(Map<CompileParameter, String> compileParametersParsed) throws MojoExecutionException {
		for (Map.Entry<CompileParameter, String> kv : compileParametersParsed.entrySet()) {
			CompileParameter cp = kv.getKey();
			String value = kv.getValue();

			if (cp instanceof TempPath) {
				compileParametersParsed.put(cp, createDirIfNotExists(value));
			}

            /*
			if(cp instanceof DslPath) {
                compileParametersParsed.put(cp, resourceAbsolutePath(value));
            }

            if(cp instanceof SqlPath) {
                compileParametersParsed.put(cp, resourceAbsolutePath(value));
            }*/
		}
	}

	public static void cleanupParameters(Map<CompileParameter, String> compileParametersParsed) {
		// Disallow custom temp path
		Iterator<Map.Entry<CompileParameter, String>> it = compileParametersParsed.entrySet().iterator();
		while (it.hasNext()) {
			// Disallow custom temp path
			if (TempPath.INSTANCE.equals(it.next().getKey())) {
				it.remove();
			}
		}
	}

	public static void copyFolder(final File sources, final File target, final Context context) throws MojoExecutionException {
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
				try {
					com.dslplatform.compiler.client.Utils.saveFile(context, tf, content.get());
				} catch (IOException ex) {
					String msg = "Error writing target file: " + tf.getAbsolutePath();
					context.error(msg);
					throw new MojoExecutionException(msg);
				}
			}
		}
	}

	public static void writeToFile(File file, String contents, String charsetName) throws MojoExecutionException {
		try {
			FileOutputStream out = new FileOutputStream(file.getAbsolutePath());
			out.write(contents.getBytes(Charset.forName(charsetName)));
			out.close();
		} catch(Exception e) {
			throw new MojoExecutionException("Error writing to file: " + file.getAbsolutePath() + ", contents: " + contents);
		}
	}
}
