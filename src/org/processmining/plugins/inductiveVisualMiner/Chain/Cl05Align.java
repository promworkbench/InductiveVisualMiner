package org.processmining.plugins.inductiveVisualMiner.Chain;

import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.model.XLog;
import org.processmining.plugins.InductiveMiner.Quintuple;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignmentETM;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignmentResult;
import org.processmining.plugins.inductiveVisualMiner.performance.XEventPerformanceClassifier;
import org.processmining.processtree.ProcessTree;

public class Cl05Align
		extends
		ChainLink<Quintuple<ProcessTree, XEventPerformanceClassifier, XLog, XEventClasses, XEventClasses>, AlignmentResult> {

	protected Quintuple<ProcessTree, XEventPerformanceClassifier, XLog, XEventClasses, XEventClasses> generateInput(
			InductiveVisualMinerState state) {
		return Quintuple.of(state.getTree(), state.getPerformanceClassifier(), state.getXLog(), state.getXLogInfo()
				.getEventClasses(), state.getXLogInfoPerformance().getEventClasses());
	}

	protected AlignmentResult executeLink(
			Quintuple<ProcessTree, XEventPerformanceClassifier, XLog, XEventClasses, XEventClasses> input) {
		return AlignmentETM.alignTree(input.getA(), input.getB(), input.getC(), input.getD(), input.getE(), canceller);
	}

	protected void processResult(AlignmentResult result, InductiveVisualMinerState state) {
		state.setAlignment(result);
	}

}