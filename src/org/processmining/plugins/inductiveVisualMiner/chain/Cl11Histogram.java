package org.processmining.plugins.inductiveVisualMiner.chain;

import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.animation.Scaler;
import org.processmining.plugins.inductiveVisualMiner.histogram.ComputeHistogram;
import org.processmining.plugins.inductiveVisualMiner.histogram.HistogramData;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLog;

public class Cl11Histogram extends ChainLink<Triple<IvMLog, Scaler, Integer>, HistogramData> {

	public Cl11Histogram(ProMCanceller globalCanceller) {
		super(globalCanceller);
	}

	protected Triple<IvMLog, Scaler, Integer> generateInput(InductiveVisualMinerState state) {
		return Triple.of((IvMLog) state.getIvMLogFiltered(), state.getAnimationScaler(), state.getHistogramWidth());
	}

	protected HistogramData executeLink(Triple<IvMLog, Scaler, Integer> input) throws Exception {
		return ComputeHistogram.computeHistogram(input.getA(), input.getB(), input.getC());
	}

	protected void processResult(HistogramData result, InductiveVisualMinerState state) {
		state.setHistogramData(result);
	}

}
