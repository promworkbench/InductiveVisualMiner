package org.processmining.plugins.inductiveVisualMiner.mode;

import java.awt.Color;

import org.processmining.plugins.graphviz.colourMaps.ColourMapFixed;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data.AlignedLogVisualisationData;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data.AlignedLogVisualisationDataImplFrequencies;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObjectValues;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogInfo;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationParameters;

public class ModePathsDeviations extends Mode {

	public ProcessTreeVisualisationParameters visualisationParameters = new ProcessTreeVisualisationParameters();

	public ModePathsDeviations() {
		visualisationParameters.setShowFrequenciesOnModelEdges(true);
		visualisationParameters.setShowFrequenciesOnMoveEdges(true);
		visualisationParameters.setColourModelEdges(new ColourMapFixed(new Color(153, 153, 255)));
		visualisationParameters.setColourMoves(new ColourMapFixed(new Color(255, 0, 0)));
	}

	@Override
	public IvMObject<?>[] createOptionalObjects() {
		return new IvMObject<?>[] {};
	}

	@Override
	public ProcessTreeVisualisationParameters getVisualisationParametersWithAlignments(IvMObjectValues inputs) {
		return visualisationParameters;
	}

	@Override
	public String toString() {
		return "paths and deviations";
	}

	@Override
	public boolean isShowDeviations() {
		return true;
	}

	@Override
	protected IvMObject<?>[] createVisualisationDataOptionalObjects() {
		return new IvMObject<?>[] { IvMObject.model, IvMObject.aligned_log_info_filtered };
	}

	@Override
	public AlignedLogVisualisationData getVisualisationData(IvMObjectValues inputs) {
		IvMModel model = inputs.get(IvMObject.model);
		IvMLogInfo logInfo = inputs.get(IvMObject.aligned_log_info_filtered);

		return new AlignedLogVisualisationDataImplFrequencies(model, logInfo);
	}

	@Override
	public boolean isVisualisationDataUpdateWithTimeStep() {
		return false;
	}
}
