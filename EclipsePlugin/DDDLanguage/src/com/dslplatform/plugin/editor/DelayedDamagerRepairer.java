package com.dslplatform.plugin.editor;

import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.ITokenScanner;

/**
 * Schedules a single parsing job each time presentation should be updated, 
 * Job is scheduled with delay (should be in about 200-1000ms range) to avoid updates while typing
 */
public class DelayedDamagerRepairer extends DefaultDamagerRepairer {

	private final long delay;
	private final ParseJob parseJob;
	
	public DelayedDamagerRepairer(ITokenScanner scanner, final ITextPresentationUpdater updater, long delay) {
		super(scanner);
		this.delay = delay;
		com.dslplatform.plugin.Logger.info("STARTING ParseJob");
		this.parseJob = new ParseJob("Parsing DSL", this, updater);
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
