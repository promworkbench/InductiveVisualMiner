package org.processmining.plugins.inductiveVisualMiner.chain;

import org.deckfour.xes.model.XLog;
import org.processmining.cohortanalysis.cohort.Cohorts;
import org.processmining.plugins.InductiveMiner.Quadruple;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.cohorts.CohortAnalysis;
import org.processmining.plugins.inductiveVisualMiner.performance.XEventPerformanceClassifier;
import org.processmining.plugins.inductiveminer2.attributes.AttributesInfo;

public class Cl18DataAnalysisCohort
		extends IvMChainLink<Quadruple<AttributesInfo, XLog, XEventPerformanceClassifier, Boolean>, Cohorts> {

	public String getName() {
		return "cohort analysis";
	}

	protected Quadruple<AttributesInfo, XLog, XEventPerformanceClassifier, Boolean> generateInput(
			InductiveVisualMinerState state) {
		return Quadruple.of(state.getAttributesInfo(), state.getSortedXLog(), state.getPerformanceClassifier(),
				state.isCohortAnalysisEnabled());
	}

	protected Cohorts executeLink(Quadruple<AttributesInfo, XLog, XEventPerformanceClassifier, Boolean> input,
			IvMCanceller canceller) throws Exception {
		if (input.getD()) {
			return CohortAnalysis.compute(input.getA(), input.getB(), input.getC(), canceller);
		} else {
			return null;
		}
	}

	protected void processResult(Cohorts result, InductiveVisualMinerState state) {
		state.setCohortAnalysis(result);
	}

	protected void invalidateResult(InductiveVisualMinerState state) {
		state.setCohortAnalysis(null);
	}

	public String getStatusBusyMessage() {
		return "Performing cohort analysis";
	}

}