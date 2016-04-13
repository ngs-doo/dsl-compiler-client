package com.dslplatform.plugin.editor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

public class ParseJob extends Job {

	private TextPresentation presentation;
	private ITypedRegion region;
	private final ITextPresentationUpdater updater;
	private final DelayedDamagerRepairer dr;
	
	public void setPresentation (TextPresentation presentation) {
		this.presentation = presentation;
	}
	
	public void setRegion (ITypedRegion region) {
		this.region = region;
	}
	
	public ParseJob(String name, final DelayedDamagerRepairer dr, final ITextPresentationUpdater updater) {
		super(name);
		this.updater = updater;
		this.dr = dr;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		
		dr.updatePresentation(presentation, region);
		
		final Display display = PlatformUI.getWorkbench().getDisplay();

		if (display != null && !display.isDisposed()) {
			display.syncExec(new Runnable() {
				@Override
				public void run() {
					if (display.isDisposed())
						return;
					updater.updateTextPresentation(presentation);
				}
			});
		}
		return Status.OK_STATUS;
	}
}