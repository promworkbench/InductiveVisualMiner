package org.processmining.plugins.inductiveVisualMiner.chain;

import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.plugins.InductiveMiner.Quadruple;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerAnimationPanel;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.animation.Scaler;
import org.processmining.plugins.inductiveVisualMiner.histogram.HistogramData;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.processtree.ProcessTree;

public class Cl11Histogram extends ChainLink<Quadruple<ProcessTree, IvMLogFiltered, Scaler, Integer>, HistogramData> {

	public Cl11Histogram(ProMCanceller globalCanceller) {
		super(globalCanceller);
	}

	protected Quadruple<ProcessTree, IvMLogFiltered, Scaler, Integer> generateInput(InductiveVisualMinerState state) {
		return Quadruple.of(state.getTree(), (IvMLogFiltered) state.getIvMLogFiltered(), state.getAnimationScaler(),
				state.getHistogramWidth());
	}

	protected HistogramData executeLink(Quadruple<ProcessTree, IvMLogFiltered, Scaler, Integer> input) throws Exception {
		return new HistogramData(input.getA(), input.getB(), input.getC(), input.getD(),
				InductiveVisualMinerAnimationPanel.popupWidth);
	}

	protected void processResult(HistogramData result, InductiveVisualMinerState state) {
		state.setHistogramData(result);
	}

}
