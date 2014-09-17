package org.processmining.plugins.inductiveVisualMiner.colouringFilter;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import nl.tue.astar.AStarThread.Canceller;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedLog;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedLogInfo;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedTrace;
import org.processmining.plugins.inductiveVisualMiner.animation.TimestampsAdder;

public class ComputeColouringFilter {

	public static Triple<AlignedLog, AlignedLogInfo, XLog> applyColouringFilter(final AlignedLog aLog,
			final AlignedLogInfo aLogInfo, final XLog xLog, final XLogInfo xLogInfo, List<ColouringFilter> filters,
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
			return Triple.of(aLog, aLogInfo, xLog);
		}

		//make a log-projection-hashmap
		HashMap<List<XEventClass>, AlignedTrace> map = TimestampsAdder.getIMTrace2AlignedTrace(aLog);

		AlignedLog result = new AlignedLog();
		XLog resultXLog = XFactoryRegistry.instance().currentDefault().createLog();
		for (XTrace xTrace : xLog) {
			List<XEventClass> lTrace = TimestampsAdder.getTraceLogProjection(xTrace, xLogInfo);
			AlignedTrace alignedTrace = map.get(lTrace);
			if (alignedTrace == null) {
				continue;
			}

			if (canceller.isCancelled()) {
				return null;
			}

			//feed this trace to each filter
			boolean keepTrace = true;
			for (ColouringFilter filter : enabledColouringFilters) {
				keepTrace = keepTrace && filter.countInColouring(xTrace, alignedTrace);

				if (!keepTrace) {
					break;
				}
			}
			if (keepTrace) {
				result.add(alignedTrace);
				resultXLog.add(xTrace);
			}
		}

		AlignedLogInfo resultInfo = new AlignedLogInfo(result);
		return Triple.of(result, resultInfo, resultXLog);
	}
}
