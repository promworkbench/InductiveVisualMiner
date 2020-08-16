package org.processmining.plugins.inductiveVisualMiner.ivmfilter.preminingfilters;

import org.deckfour.xes.model.XEvent;

public abstract class PreMiningEventFilter extends PreMiningFilter {

	/**
	 * Main function of the filter. Returns whether the given XEvent should
	 * remain in the log.
	 * 
	 * @param event
	 * @return
	 */
	public abstract boolean staysInLog(XEvent event);

}