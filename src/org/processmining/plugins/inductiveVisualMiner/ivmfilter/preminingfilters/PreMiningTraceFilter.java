package org.processmining.plugins.inductiveVisualMiner.ivmfilter.preminingfilters;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.annotations.KeepInProMCache;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFilter;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLog;

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

	/**
	 * Update the gui with the given values. Will be called asynchronously, so
	 * synchronise on the event thread before pushing updates.
	 * 
	 * @param log
	 * @param attributeInfo
	 * @return Whether the filtering changed and an update is necessary (do not
	 *         call update() yourself).
	 * @throws Exception
	 */
	public abstract boolean fillGuiWithLog(IMLog log, XLog xLog) throws Exception;

	@Override
	protected final boolean fillGuiWithLog(IMLog log, XLog xLog, IvMLog ivmLog) throws Exception {
		return fillGuiWithLog(log, xLog);
	}
}
