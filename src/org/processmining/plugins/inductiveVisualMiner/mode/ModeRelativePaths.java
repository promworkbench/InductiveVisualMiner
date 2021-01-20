package org.processmining.plugins.inductiveVisualMiner.mode;

import org.processmining.plugins.graphviz.colourMaps.ColourMapLightGreen;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data.AlignedLogVisualisationData;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data.AlignedLogVisualisationDataImplRelativeFrequencies;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObjectValues;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.sizeMaps.SizeMapLinear;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogInfo;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationParameters;

public class ModeRelativePaths extends Mode {

	public ProcessTreeVisualisationParameters visualisationParameters = new ProcessTreeVisualisationParameters();

	public ModeRelativePaths() {
		visualisationParameters.setShowFrequenciesOnModelEdges(true);
		visualisationParameters.setColourModelEdges(new ColourMapLightGreen());
		visualisationParameters.setModelEdgesWidth(new SizeMapLinear(1, 3));
		visualisationParameters.setShowFrequenciesOnMoveEdges(false);
		visualisationParameters.setShowLogMoves(false);
		visualisationParameters.setShowModelMoves(false);

		visualisationParameters.setColourNodes(new ColourMapLightGreen());
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
		return "relative paths";
	}

	@Override
	public boolean isShowDeviations() {
		return false;
	}

	@Override
	protected IvMObject<?>[] createVisualisationDataOptionalObjects() {
		return new IvMObject<?>[] { IvMObject.model, IvMObject.aligned_log_info_filtered };
	}

	@Override
	public AlignedLogVisualisationData getVisualisationData(IvMObjectValues inputs) {
		IvMModel model = inputs.get(IvMObject.model);
		IvMLogInfo logInfo = inputs.get(IvMObject.aligned_log_info_filtered);

		return new AlignedLogVisualisationDataImplRelativeFrequencies(model, logInfo);
	}

	@Override
	public boolean isVisualisationDataUpdateWithTimeStep() {
		return false;
	}
}
