package org.processmining.plugins.inductiveVisualMiner.chain;

import org.processmining.plugins.InductiveMiner.Quadruple;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerAnimationPanel;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.animation.Scaler;
import org.processmining.plugins.inductiveVisualMiner.histogram.HistogramData;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.processtree.ProcessTree;

public class Cl11Histogram extends ChainLink<Quadruple<ProcessTree, IvMLogFiltered, Scaler, Integer>, HistogramData> {

	protected Quadruple<ProcessTree, IvMLogFiltered, Scaler, Integer> generateInput(InductiveVisualMinerState state) {
		return Quadruple.of(state.getTree(), (IvMLogFiltered) state.getIvMLogFiltered(), state.getAnimationScaler(),
				state.getHistogramWidth());
	}

	protected HistogramData executeLink(Quadruple<ProcessTree, IvMLogFiltered, Scaler, Integer> input,
			ChainLinkCanceller canceller) throws Exception {
		HistogramData data = new HistogramData(input.getA(), input.getB(), input.getC(), input.getD(),
				InductiveVisualMinerAnimationPanel.popupWidth, canceller);
		if (canceller.isCancelled()) {
			return null;
		}
		return data;
	}

	protected void processResult(HistogramData result, InductiveVisualMinerState state) {
		state.setHistogramData(result);
	}

}
