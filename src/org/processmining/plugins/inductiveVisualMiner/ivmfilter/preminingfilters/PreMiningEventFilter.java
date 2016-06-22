package org.processmining.plugins.inductiveVisualMiner.ivmfilter.preminingfilters;

import org.deckfour.xes.model.XEvent;
import org.processmining.framework.plugin.annotations.KeepInProMCache;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFilter;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLog;

@KeepInProMCache
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

	/**
	 * Update the gui with the given values. Will be called asynchronously, so
	 * synchronise on the event thread before pushing updates.
	 * 
	 * @param log
	 * @param attributeInfo
	 * @return Whether the filter selection changed (this will cause an
	 *         update-round; do not call update() yourself).
	 * @throws Exception
	 */
	public abstract boolean fillGuiWithLog(IMLog log) throws Exception;

	@Override
	protected final boolean fillGuiWithLog(IMLog log, IvMLog ivmLog) throws Exception {
		return fillGuiWithLog(log);
	}
}
