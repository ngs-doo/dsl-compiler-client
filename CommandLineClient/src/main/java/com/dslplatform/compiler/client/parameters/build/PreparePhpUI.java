package com.dslplatform.compiler.client.parameters.build;

import com.dslplatform.compiler.client.Context;
import com.dslplatform.compiler.client.ExitException;

import java.io.File;

public class PreparePhpUI extends PrepareSources {

	public PreparePhpUI(
			final String targetName,
			final String targetId,
			final String targetFolder) {
		super(targetName, targetId, targetFolder);
	}

	// @TODO: temp fix for missing .php extensions, should be removed when fixed in the compiler
	private void fixMissingExtensions(final File sources, final Context context) {
		final String[] children = sources.list();
		if (children == null) return;
		for (final String fn : children) {
			final File sf = new File(sources, fn);
			if (sf.isFile() && sf.getName().endsWith(".twig.php")) {
				final String twigOnly =  sf.getName().substring(0, sf.getName().length() - 4);
				if (!sf.renameTo(new File(sf.getParentFile(), twigOnly))) {
					context.error("Error renaming file " + sf.getAbsolutePath() + " to : " + twigOnly);
				}
			} else if (sf.isDirectory()) {
				fixMissingExtensions(sf, context);
			}
		}
	}

	@Override
	public void build(final File sources, final Context context) throws ExitException {
		fixMissingExtensions(sources, context);
		super.build(sources, context);
	}
}
