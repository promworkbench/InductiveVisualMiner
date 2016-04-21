package org.processmining.plugins.inductiveVisualMiner.logFiltering;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.InductiveMiner.dfgOnly.log2logInfo.IMLog2IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace.IMEventIterator;

public class FilterLeastOccurringActivities {
	public static Triple<IMLog, IMLogInfo, Set<XEventClass>> filter(IMLog log, IMLogInfo logInfo, double threshold,
			IMLog2IMLogInfo log2logInfo) {
		List<XEventClass> list = logInfo.getActivities().sortByCardinality();
		int lastIndex = (int) Math.floor((1 - threshold) * list.size());
		Set<XEventClass> remove = new HashSet<XEventClass>(list.subList(0, lastIndex));

		IMLog newLog = log.clone();
		for (IMTrace trace : newLog) {
			for (IMEventIterator it = trace.iterator(); it.hasNext(); ) {
				it.next();
				if (remove.contains(it.classify())) {
					it.remove();
				}
			}
		}

		IMLogInfo filteredLogInfo = log2logInfo.createLogInfo(newLog);

		return Triple.of(newLog, filteredLogInfo, remove);
	}
}
