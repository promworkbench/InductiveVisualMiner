package org.processmining.plugins.inductiveVisualMiner.chain;

import org.processmining.plugins.InductiveMiner.Quintuple;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.animation.Scaler;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.popup.HistogramData;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupPopulator;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationInfo;

public class Cl14Histogram extends
		IvMChainLink<Quintuple<IvMModel, IvMLogFiltered, Scaler, Integer, ProcessTreeVisualisationInfo>, HistogramData> {

	protected Quintuple<IvMModel, IvMLogFiltered, Scaler, Integer, ProcessTreeVisualisationInfo> generateInput(
			InductiveVisualMinerState state) {
		if (!state.isIllogicalTimeStamps()) {
			return Quintuple.of(state.getModel(), (IvMLogFiltered) state.getIvMLogFiltered(),
					state.getAnimationScaler(), state.getHistogramWidth(), state.getVisualisationInfo());
		} else {
			return null;
		}
	}

	protected HistogramData executeLink(
			Quintuple<IvMModel, IvMLogFiltered, Scaler, Integer, ProcessTreeVisualisationInfo> input,
			IvMCanceller canceller) throws Exception {
		if (input != null) {

			IvMModel model = input.getA();
			if (model == null) {
				return null;
			}
			if (input.getD() <= 0) {
				return null;
			}
			HistogramData data = new HistogramData(model, input.getE(), input.getB(), input.getC(), input.getD(),
					PopupPopulator.popupWidthNodes, PopupPopulator.popupWidthSourceSink, canceller);
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

	public String getName() {
		return "compute histograms";
	}

	public String getStatusBusyMessage() {
		return "Computing histogram..";
	}
}
