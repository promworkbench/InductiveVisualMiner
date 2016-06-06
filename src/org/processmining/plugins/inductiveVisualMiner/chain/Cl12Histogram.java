package org.processmining.plugins.inductiveVisualMiner.chain;

import org.processmining.plugins.InductiveMiner.Quadruple;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerAnimationPanel;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.animation.Scaler;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMEfficientTree;
import org.processmining.plugins.inductiveVisualMiner.histogram.HistogramData;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;

public class Cl12Histogram extends
		ChainLink<Quadruple<IvMEfficientTree, IvMLogFiltered, Scaler, Integer>, HistogramData> {

	protected Quadruple<IvMEfficientTree, IvMLogFiltered, Scaler, Integer> generateInput(InductiveVisualMinerState state) {
		if (!state.isIllogicalTimeStamps()) {
			return Quadruple.of(state.getTree(), (IvMLogFiltered) state.getIvMLogFiltered(),
					state.getAnimationScaler(), state.getHistogramWidth());
		} else {
			return null;
		}
	}

	protected HistogramData executeLink(Quadruple<IvMEfficientTree, IvMLogFiltered, Scaler, Integer> input,
			IvMCanceller canceller) throws Exception {
		if (input != null) {

			IvMEfficientTree tree = input.getA();
			if (tree == null) {
				return null;
			}
			if (input.getD() <= 0) {
				return null;
			}
			HistogramData data = new HistogramData(tree, input.getB(), input.getC(), input.getD(),
					InductiveVisualMinerAnimationPanel.popupWidth, canceller);
			if (canceller.isCancelled()) {
				return null;
			}
			return data;
		} else {
			return null;
		}
	}

	protected void processResult(HistogramData result, InductiveVisualMinerState state) {
		state.setHistogramData(result);
	}

}
