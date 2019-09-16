package org.processmining.plugins.inductiveVisualMiner.chain;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysis;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.AttributesInfo;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;

public class Cl15DataAnalysis extends ChainLink<Pair<IvMLogFiltered, AttributesInfo>, DataAnalysis> {

	public String getName() {
		return "data analysis";
	}

	protected Pair<IvMLogFiltered, AttributesInfo> generateInput(InductiveVisualMinerState state) {
		return Pair.of((IvMLogFiltered) state.getIvMLogFiltered(), state.getAttributesInfo());
	}

	protected DataAnalysis executeLink(Pair<IvMLogFiltered, AttributesInfo> input, IvMCanceller canceller)
			throws Exception {
		return new DataAnalysis(input.getA(), input.getB());
	}

	protected void processResult(DataAnalysis result, InductiveVisualMinerState state) {
		state.setDataAnalysis(result);
	}

	protected void invalidateResult(InductiveVisualMinerState state) {
		state.setDataAnalysis(null);
	}

}