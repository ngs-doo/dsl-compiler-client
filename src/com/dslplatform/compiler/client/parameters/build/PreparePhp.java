package com.dslplatform.compiler.client.parameters.build;

import com.dslplatform.compiler.client.*;

import java.io.File;
import java.io.IOException;

public class PreparePhp implements BuildAction {

	private static final String CACHE_NAME = "php_target_folder";

	@Override
	public boolean check(final Context context) throws ExitException {
		final File target = new File("Generated-PHP");
		if (target.exists() && target.isDirectory()) {
			try {
				Utils.deletePath(target);
			} catch (IOException ex) {
				context.error("Failed to clean PHP target folder: " + target.getAbsolutePath());
				context.error(ex);
				throw new ExitException();
			}
		} else if (target.exists() && !target.isDirectory()) {
			context.error("Expecting to find folder. Found file at: " + target.getAbsolutePath());
			throw new ExitException();
		} else if (!target.mkdirs()) {
			context.error("Failed to create PHP target folder: " + target.getAbsolutePath());
			throw new ExitException();
		}
		context.cache(CACHE_NAME, target);
		return true;
	}

	private static void copyFolder(final File sources, final File target, final Context context) throws ExitException {
		for (final String fn : sources.list()) {
			final File sf = new File(sources, fn);
			final File tf = new File(target, fn);
			if (sf.isDirectory()) {
				if (!tf.mkdirs()) {
					context.error("Failed to create target PHP folder: " + tf.getAbsolutePath());
					throw new ExitException();
				}
			} else {
				final Either<String> content = Utils.readFile(sf);
				if (!content.isSuccess()) {
					context.error("Error reading source PHP file: " + sf.getAbsolutePath());
					throw new ExitException();
				}
				try {
					Utils.saveFile(tf, content.get());
				} catch (IOException ex) {
					context.error("Error writing target PHP file: " + tf.getAbsolutePath());
					throw new ExitException();
				}
			}
		}
	}

	@Override
	public void build(final File sources, final Context context) throws ExitException {
		final File target = context.load(CACHE_NAME);
		copyFolder(sources, target, context);
	}
}
