package org.processmining.plugins.inductiveVisualMiner.chain;

import org.processmining.plugins.inductiveVisualMiner.animation.ComputeAnimation;
import org.processmining.plugins.inductiveVisualMiner.animation.GraphVizTokens;
import org.processmining.plugins.inductiveVisualMiner.animation.Scaler;
import org.processmining.plugins.inductiveVisualMiner.configuration.InductiveVisualMinerConfiguration;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFiltered;
import org.processmining.plugins.inductiveVisualMiner.mode.Mode;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationInfo;

import com.kitfox.svg.SVGDiagram;

public class Cl11Animate extends DataChainLinkComputationAbstract<InductiveVisualMinerConfiguration> {

	@Override
	public String getName() {
		return "animate";
	}

	@Override
	public String getStatusBusyMessage() {
		return "Creating animation..";
	}

	@Override
	public IvMObject<?>[] createInputObjects() {
		return new IvMObject<?>[] { IvMObject.selected_animation_enabled, IvMObject.aligned_log,
				IvMObject.log_timestamps_logical, IvMObject.selected_visualisation_mode,
				IvMObject.graph_visualisation_info_aligned, IvMObject.graph_svg_aligned, IvMObject.animation_scaler,
				IvMObject.model };
	}

	@Override
	public IvMObject<?>[] createOutputObjects() {
		return new IvMObject<?>[] { IvMObject.animation };
	}

	@Override
	public IvMObjectValues execute(InductiveVisualMinerConfiguration configuration, IvMObjectValues inputs,
			IvMCanceller canceller) throws Exception {
		boolean animationEnabled = inputs.get(IvMObject.selected_animation_enabled);
		IvMLogNotFiltered aLog = inputs.get(IvMObject.aligned_log);
		Boolean timestampsLogical = inputs.get(IvMObject.log_timestamps_logical);
		Mode mode = inputs.get(IvMObject.selected_visualisation_mode);
		ProcessTreeVisualisationInfo visualisationInfo = inputs.get(IvMObject.graph_visualisation_info_aligned);
		SVGDiagram svg = inputs.get(IvMObject.graph_svg_aligned);
		Scaler scaler = inputs.get(IvMObject.animation_scaler);
		IvMModel model = inputs.get(IvMObject.model);

		if (animationEnabled && timestampsLogical) {
			GraphVizTokens result = ComputeAnimation.computeAnimation(model, aLog, mode, visualisationInfo, scaler, svg,
					canceller);
			return new IvMObjectValues().//
					s(IvMObject.animation, result);
		} else {
			return null;
		}
	}
}