package org.processmining.plugins.inductiveVisualMiner.chain;

import org.processmining.plugins.inductiveVisualMiner.animation.Scaler;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFilteredImpl;
import org.processmining.plugins.inductiveVisualMiner.popup.HistogramData;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupController;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationInfo;

public class Cl15Histogram<C> extends DataChainLinkComputationAbstract<C> {

	@Override
	public String getName() {
		return "compute histograms";
	}

	@Override
	public String getStatusBusyMessage() {
		return "Computing histogram..";
	}

	@Override
	public IvMObject<?>[] createInputObjects() {
		return new IvMObject<?>[] { IvMObject.model, IvMObject.aligned_log_filtered, IvMObject.animation_scaler,
				IvMObject.histogram_width, IvMObject.graph_visualisation_info_aligned };
	}

	@Override
	public IvMObject<?>[] createOutputObjects() {
		return new IvMObject<?>[] { IvMObject.histogram_data };
	}

	@Override
	public IvMObjectValues execute(C configuration, IvMObjectValues inputs, IvMCanceller canceller) throws Exception {
		IvMModel model = inputs.get(IvMObject.model);
		IvMLogFilteredImpl aLog = inputs.get(IvMObject.aligned_log_filtered);
		Scaler scaler = inputs.get(IvMObject.animation_scaler);
		int width = inputs.get(IvMObject.histogram_width);
		ProcessTreeVisualisationInfo info = inputs.get(IvMObject.graph_visualisation_info_aligned);

		if (width <= 0) {
			return null;
		}
		HistogramData data = new HistogramData(model, info, aLog, scaler, width, PopupController.popupWidthNodes,
				PopupController.popupWidthSourceSink, canceller);

		return new IvMObjectValues().//
				s(IvMObject.histogram_data, data);

	}

}
