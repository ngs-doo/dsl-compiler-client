package com.dslplatform.compiler.client.parameters.build;

import com.dslplatform.compiler.client.Context;
import com.dslplatform.compiler.client.Either;
import com.dslplatform.compiler.client.ExitException;
import com.dslplatform.compiler.client.Utils;

import java.io.File;
import java.io.IOException;

public class PreparePhpUI extends PrepareSources {

	public PreparePhpUI(
			final String targetName,
			final String targetId,
			final String targetFolder) {
		super(targetName, targetId, targetFolder);
	}

    // @todo temp fix for missing .php extensions, should be removed when fixed in compiler
    private void fixMissingExtensions(final File sources, final Context context) {
        for (final String fn : sources.list()) {
            final File sf = new File(sources, fn);
            if (sf.isFile() && !sf.getName().endsWith(".twig")) {
                if (!sf.renameTo(new File(sources, fn.concat(".php")))) {
                    context.error("Error renaming file " + sf.getAbsolutePath() + " to : " + sf.getName() + ".php");
                }
            }
            else if (sf.isDirectory()) {
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
