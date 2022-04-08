package org.processmining.plugins.inductiveVisualMiner.causal;

import java.util.ArrayList;
import java.util.List;

import org.processmining.directlyfollowsmodelminer.model.DirectlyFollowsModel;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IteratorWithPosition;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.set.TIntSet;

public class DirectlyFollowsModel2StepsGraph {
	public static StepsGraph create(DirectlyFollowsModel dfm, IvMLogFiltered log, TIntObjectMap<TIntSet> node2steps) {
		final StepsGraph result = new StepsGraph();

		final List<TIntSet> traceHistory = new ArrayList<>();

		DirectlyFollowsModelStepsWalk walk = new DirectlyFollowsModelStepsWalk(dfm, node2steps) {
			public void stepsEncountered(TIntSet steps, int chosenNode, TIntSet nextSteps) {
				if (nextSteps.size() > 0) {
					traceHistory.add(steps);
					for (TIntSet s : traceHistory) {
						result.edges.add(Pair.of(s, nextSteps));
					}
				}
			}
		};

		for (IteratorWithPosition<IvMTrace> it = log.iterator(); it.hasNext();) {
			traceHistory.clear();
			walk.walk(it.next());
		}

		return result;
	}
}