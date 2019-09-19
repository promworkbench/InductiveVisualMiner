package org.processmining.plugins.inductiveVisualMiner.chain;

import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysis;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.AttributesInfo;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFiltered;

public class Cl15DataAnalysis
		extends ChainLink<Triple<IvMLogNotFiltered, IvMLogFiltered, AttributesInfo>, DataAnalysis> {

	public String getName() {
		return "data analysis";
	}

	protected Triple<IvMLogNotFiltered, IvMLogFiltered, AttributesInfo> generateInput(InductiveVisualMinerState state) {
		return Triple.of(state.getIvMLog(), (IvMLogFiltered) state.getIvMLogFiltered(), state.getAttributesInfo());
	}

	protected DataAnalysis executeLink(Triple<IvMLogNotFiltered, IvMLogFiltered, AttributesInfo> input,
			IvMCanceller canceller) throws Exception {
		return new DataAnalysis(input.getA(), input.getB(), input.getC(), canceller);
	}

	protected void processResult(DataAnalysis result, InductiveVisualMinerState state) {
		state.setDataAnalysis(result);
	}

	protected void invalidateResult(InductiveVisualMinerState state) {
		state.setDataAnalysis(null);
	}

}