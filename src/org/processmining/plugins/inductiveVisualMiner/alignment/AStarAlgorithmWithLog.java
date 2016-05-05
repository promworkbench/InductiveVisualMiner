package org.processmining.plugins.inductiveVisualMiner.alignment;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.THashMap;

import java.util.Map;

import nl.tue.astar.Trace;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.model.XLog;
import org.processmining.plugins.etm.model.narytree.replayer.AStarAlgorithm;

/**
 * Class to keep track of which trace is used where in the event log
 * 
 * @author sleemans
 *
 */
public class AStarAlgorithmWithLog extends AStarAlgorithm {

	private Map<Trace, TIntArrayList> atrace2xtrace;

	public AStarAlgorithmWithLog(XLog log, IvMEventClasses classes, Map<XEventClass, Integer> activity2Cost) {
		super(log, classes, activity2Cost);

		atrace2xtrace = new THashMap<>();

		for (int j = 0; j < log.size(); j++) {
			Trace list = getListEventClass(log, j);
			TIntArrayList occurrences;
			if (atrace2xtrace.containsKey(list)) {
				occurrences = atrace2xtrace.get(list);
			} else {
				occurrences = new TIntArrayList();
				atrace2xtrace.put(list, occurrences);
			}
			occurrences.add(j);
		}
	}

	public TIntArrayList getXTracesOf(Trace aTrace) {
		return atrace2xtrace.get(aTrace);
	}
}
