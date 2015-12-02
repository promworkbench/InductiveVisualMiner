package org.processmining.plugins.inductiveVisualMiner.chain;

import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.animation.Scaler;
import org.processmining.plugins.inductiveVisualMiner.histogram.HistogramData;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;

public class Cl11Histogram extends ChainLink<Triple<IvMLogFiltered, Scaler, Integer>, HistogramData> {

	public Cl11Histogram(ProMCanceller globalCanceller) {
		super(globalCanceller);
	}

	protected Triple<IvMLogFiltered, Scaler, Integer> generateInput(InductiveVisualMinerState state) {
		return Triple.of((IvMLogFiltered) state.getIvMLogFiltered(), state.getAnimationScaler(),
				state.getHistogramWidth());
	}

	protected HistogramData executeLink(Triple<IvMLogFiltered, Scaler, Integer> input) throws Exception {
		return new HistogramData(input.getA(), input.getB(), input.getC());
	}

	protected void processResult(HistogramData result, InductiveVisualMinerState state) {
		state.setHistogramData(result);
	}

}
