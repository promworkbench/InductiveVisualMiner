package org.processmining.plugins.inductiveVisualMiner.ivmfilter.preminingfilters;

import org.deckfour.xes.model.XEvent;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFilter;

@PreMiningEventFilterAnnotation
public abstract class PreMiningEventFilter extends IvMFilter {

	/**
	 * Main function of the filter. Returns whether the given XEvent should
	 * remain in the log.
	 * 
	 * @param event
	 * @return
	 */
	public abstract boolean staysInLog(XEvent event);
}
