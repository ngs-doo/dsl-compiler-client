package com.dslplatform.compiler.client.parameters.build;

import com.dslplatform.compiler.client.*;

import java.io.File;
import java.io.IOException;

public class PrepareSources implements BuildAction {

	private final String targetName;
	private final String targetId;
	private final String targetFolder;

	public PrepareSources(
			final String targetName,
			final String targetId,
			final String targetFolder) {
		this.targetName = targetName;
		this.targetId = targetId;
		this.targetFolder = targetFolder;
	}

	@Override
	public boolean check(final Context context) throws ExitException {
		final String customFolder = context.get(targetId);
		final File target = new File(customFolder != null ? customFolder : targetFolder);
		if (target.exists() && target.isDirectory()) {
			try {
				Utils.deletePath(target);
			} catch (IOException ex) {
				context.error("Failed to clean " + targetName + " target folder: " + target.getAbsolutePath());
				context.error(ex);
				throw new ExitException();
			}
		} else if (target.exists() && !target.isDirectory()) {
			context.error("Expecting to find folder. Found file at: " + target.getAbsolutePath());
			throw new ExitException();
		} else if (!target.mkdirs()) {
			context.error("Failed to create " + targetName + " target folder: " + target.getAbsolutePath());
			throw new ExitException();
		}
		context.cache(targetId, target);
		return true;
	}

	private void copyFolder(final File sources, final File target, final Context context) throws ExitException {
		for (final String fn : sources.list()) {
			final File sf = new File(sources, fn);
			final File tf = new File(target, fn);
			if (sf.isDirectory()) {
				if (!tf.mkdirs()) {
					context.error("Failed to create target " + targetName + " folder: " + tf.getAbsolutePath());
					throw new ExitException();
				}
				copyFolder(sf, tf, context);
			} else {
				final Either<String> content = Utils.readFile(sf);
				if (!content.isSuccess()) {
					context.error("Error reading source " + targetName + " file: " + sf.getAbsolutePath());
					throw new ExitException();
				}
				try {
					Utils.saveFile(context, tf, content.get());
				} catch (IOException ex) {
					context.error("Error writing target PHP file: " + tf.getAbsolutePath());
					throw new ExitException();
				}
			}
		}
	}

	@Override
	public void build(final File sources, final Context context) throws ExitException {
		final File target = context.load(targetId);
		copyFolder(sources, target, context);
	}
}
