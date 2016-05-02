package org.processmining.plugins.inductiveVisualMiner.ivmfilter.preminingfilters;

import org.processmining.framework.plugin.annotations.KeepInProMCache;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFilter;

@KeepInProMCache
@PreMiningEventFilterAnnotation
public abstract class PreMiningTraceFilter extends IvMFilter {

	/**
	 * Main function of the filter. Returns whether the given XEvent should
	 * remain in the log.
	 * 
	 * @param trace
	 * @return
	 */
	public abstract boolean staysInLog(IMTrace trace);
}
