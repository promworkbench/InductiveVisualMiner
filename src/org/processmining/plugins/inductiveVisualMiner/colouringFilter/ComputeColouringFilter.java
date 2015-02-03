package org.processmining.plugins.inductiveVisualMiner.colouringFilter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import nl.tue.astar.AStarThread.Canceller;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.info.XLogInfo;
import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.InductiveMiner.mining.IMLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedLog;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedLogInfo;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedTrace;
import org.processmining.plugins.inductiveVisualMiner.animation.TimestampsAdder;

public class ComputeColouringFilter {

	/**
	 * Apply colouring filters.
	 * 
	 * @param aLog
	 * @param aLogInfo
	 * @param xLog
	 * @param xLogInfo
	 * @param filters
	 * @param canceller
	 * @return
	 */
	public static Triple<AlignedLog, AlignedLogInfo, IMLog> applyColouringFilter(final AlignedLog aLog,
			final AlignedLogInfo aLogInfo, final IMLog log, final XLogInfo xLogInfo, List<ColouringFilter> filters,
			final Canceller canceller) {

		//first, walk through the filters to see there is actually one enabled
		List<ColouringFilter> enabledColouringFilters = new LinkedList<>();
		for (ColouringFilter filter : filters) {
			if (filter.isEnabledFilter()) {
				enabledColouringFilters.add(filter);
			}
		}
		if (enabledColouringFilters.isEmpty()) {
			//no filter is enabled, just return the unchanged aligned log
			return Triple.of(aLog, aLogInfo, log);
		}

		//make a log-projection-hashmap
		HashMap<List<XEventClass>, AlignedTrace> map = TimestampsAdder.getIMTrace2AlignedTrace(aLog);

		AlignedLog resultALog = new AlignedLog();
		IMLog resultLog = new IMLog(log);
		for (Iterator<IMTrace> it = resultLog.iterator(); it.hasNext();) {
			IMTrace trace = it.next();
			List<XEventClass> lTrace = TimestampsAdder.getTraceLogProjection(trace, xLogInfo);
			AlignedTrace alignedTrace = map.get(lTrace);
			if (alignedTrace == null) {
				continue;
			}

			if (canceller.isCancelled()) {
				return null;
			}

			//feed this trace to each enabled filter
			boolean keepTrace = true;
			for (ColouringFilter filter : enabledColouringFilters) {
				keepTrace = keepTrace && filter.countInColouring(trace, alignedTrace);

				if (!keepTrace) {
					break;
				}
			}

			if (keepTrace) {
				resultALog.add(alignedTrace);
			} else {
				it.remove();
			}
		}

		AlignedLogInfo resultInfo = new AlignedLogInfo(resultALog);
		return Triple.of(resultALog, resultInfo, resultLog);
	}
}
