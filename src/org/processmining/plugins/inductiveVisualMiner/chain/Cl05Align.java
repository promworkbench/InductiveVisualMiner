package org.processmining.plugins.inductiveVisualMiner.chain;

import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.model.XLog;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.Sextuple;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignmentPerformance;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMEfficientTree;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogInfo;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFiltered;
import org.processmining.plugins.inductiveVisualMiner.performance.XEventPerformanceClassifier;

public class Cl05Align
		extends
		ChainLink<Sextuple<IvMEfficientTree, XEventPerformanceClassifier, XLog, IMLog, XEventClasses, XEventClasses>, Pair<IvMLogNotFiltered, IvMLogInfo>> {

	protected Sextuple<IvMEfficientTree, XEventPerformanceClassifier, XLog, IMLog, XEventClasses, XEventClasses> generateInput(
			InductiveVisualMinerState state) {
		return Sextuple.of(state.getTree(), state.getPerformanceClassifier(), state.getXLog(), state.getLog(), state
				.getXLogInfo().getEventClasses(), state.getXLogInfoPerformance().getEventClasses());
	}

	protected Pair<IvMLogNotFiltered, IvMLogInfo> executeLink(
			Sextuple<IvMEfficientTree, XEventPerformanceClassifier, XLog, IMLog, XEventClasses, XEventClasses> input,
			IvMCanceller canceller) throws Exception {
		IvMEfficientTree tree = input.getA();

		IvMLogNotFiltered log = AlignmentPerformance.align(tree, input.getB(), input.getC(), input.getD(),
				input.getE(), input.getF(), canceller);
		if (log == null) {
			return null;
		}
		IvMLogInfo logInfo = new IvMLogInfo(log, tree);
		return Pair.of(log, logInfo);
	}

	protected void processResult(Pair<IvMLogNotFiltered, IvMLogInfo> result, InductiveVisualMinerState state) {
		state.setIvMLog(result.getA(), result.getB());
	}

}