package com.dslplatform.plugin.editor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

/**
 * Schedules a single parsing job each time presentation should be updated, 
 * Job is scheduled with delay (should be in about 200-1000ms range) to avoid updates while typing
 */
public class DelayedDamagerRepairer extends DefaultDamagerRepairer {

	private final ITextPresentationUpdater updater;
	private final long delay;
	private final ParseJob parseJob;
	
	private class ParseJob extends Job {
		
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
	
	public DelayedDamagerRepairer(ITokenScanner scanner, final ITextPresentationUpdater updater, long delay) {
		super(scanner);
		this.updater = updater;
		this.delay = delay;
		this.parseJob = new ParseJob("DSL parsing", this, updater);
	}

	 /*
	 * @see IPresentationRepairer#createPresentation(TextPresentation, ITypedRegion)
	 */
	@Override
	public void createPresentation (TextPresentation presentation, ITypedRegion region) {
		this.parseJob.setPresentation(presentation);
		this.parseJob.setRegion(region);
		this.parseJob.cancel();
		// if job is not running, effectively reset scheduled time
		this.parseJob.schedule(this.delay);
	}
		
	public void updatePresentation (TextPresentation presentation, ITypedRegion region) {
		super.createPresentation(presentation, region);
	}
}
