package com.dslplatform.compiler.client.parameters.build;

import com.dslplatform.compiler.client.*;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

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
		if (target.exists() && !target.isDirectory()) {
			context.error("Expecting to find folder. Found file at: " + target.getAbsolutePath());
			throw new ExitException();
		} else if (!target.exists() && !target.mkdirs()) {
			context.error("Failed to create " + targetName + " target folder: " + target.getAbsolutePath());
			throw new ExitException();
		}
		context.cache(targetId, target);
		return true;
	}

	private void copyFolder(final File sources, final File target, final Context context) throws ExitException {
		final String[] children = sources.list();
		if (children == null) return;
		final HashSet<String> processed = new HashSet<String>();
		for (final String fn : children) {
			final File sf = new File(sources, fn);
			final File tf = new File(target, fn);
			processed.add(fn);
			if (sf.isDirectory()) {
				if (tf.exists() && !tf.isDirectory() && !tf.delete()) {
					context.error("Failed to delete old " + targetName + " file: " + tf.getAbsolutePath());
					throw new ExitException();
				} else if (!tf.exists() && !tf.mkdirs()) {
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
				if (tf.exists()) {
					final Either<String> oldContent = Utils.readFile(tf);
					if (!oldContent.isSuccess()) {
						context.error("Error reading target " + targetName + " file: " + tf.getAbsolutePath());
						throw new ExitException();
					}
					if (oldContent.get().equals(content.get())) continue;
				}
				try {
					Utils.saveFile(context, tf, content.get());
				} catch (IOException ex) {
					context.error("Error writing target " + targetName + " file: " + tf.getAbsolutePath());
					throw new ExitException();
				}
			}
		}
		final String[] existing = target.list();
		if (existing == null) return;
		for (final String fn : existing) {
			if (processed.contains(fn)) continue;
			final File tf = new File(target, fn);
			if (tf.isDirectory()) {
				try {
					Utils.deletePath(tf);
				} catch (IOException ex) {
					context.error("Error deleting old " + targetName + " folder: " + tf.getAbsolutePath());
					throw new ExitException();
				}
				if (tf.exists() && !tf.delete()) {
					context.error("Failed to delete old " + targetName + " folder: " + tf.getAbsolutePath());
					throw new ExitException();
				}
			} else if (!tf.delete()) {
				context.error("Failed to delete old " + targetName + " file: " + tf.getAbsolutePath());
				throw new ExitException();
			}
		}
	}

	@Override
	public void build(final File sources, final Context context) throws ExitException {
		final File target = context.load(targetId);
		copyFolder(sources, target, context);
	}
}
