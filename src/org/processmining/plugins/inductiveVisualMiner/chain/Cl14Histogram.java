package org.processmining.plugins.inductiveVisualMiner.chain;

import org.processmining.plugins.InductiveMiner.Quintuple;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerAnimationPanel;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.animation.Scaler;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMEfficientTree;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.popup.HistogramData;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationInfo;

public class Cl14Histogram extends
		ChainLink<Quintuple<IvMEfficientTree, IvMLogFiltered, Scaler, Integer, ProcessTreeVisualisationInfo>, HistogramData> {

	protected Quintuple<IvMEfficientTree, IvMLogFiltered, Scaler, Integer, ProcessTreeVisualisationInfo> generateInput(
			InductiveVisualMinerState state) {
		if (!state.isIllogicalTimeStamps()) {
			return Quintuple.of(state.getTree(), (IvMLogFiltered) state.getIvMLogFiltered(), state.getAnimationScaler(),
					state.getHistogramWidth(), state.getVisualisationInfo());
		} else {
			return null;
		}
	}

	protected HistogramData executeLink(
			Quintuple<IvMEfficientTree, IvMLogFiltered, Scaler, Integer, ProcessTreeVisualisationInfo> input,
			IvMCanceller canceller) throws Exception {
		if (input != null) {

			IvMEfficientTree tree = input.getA();
			if (tree == null) {
				return null;
			}
			if (input.getD() <= 0) {
				return null;
			}
			HistogramData data = new HistogramData(tree, input.getE(), input.getB(), input.getC(), input.getD(),
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

	protected void invalidateResult(InductiveVisualMinerState state) {
		state.setHistogramData(null);
	}

}
