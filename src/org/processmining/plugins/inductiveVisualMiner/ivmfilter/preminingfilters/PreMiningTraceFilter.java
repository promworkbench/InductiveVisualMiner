package org.processmining.plugins.inductiveVisualMiner.ivmfilter.preminingfilters;

import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;

public abstract class PreMiningTraceFilter extends PreMiningFilter {

	/**
	 * Main function of the filter. Returns whether the given IMTrace should
	 * remain in the log.
	 * 
	 * @param trace
	 * @return
	 */
	public abstract boolean staysInLog(IMTrace trace);

}