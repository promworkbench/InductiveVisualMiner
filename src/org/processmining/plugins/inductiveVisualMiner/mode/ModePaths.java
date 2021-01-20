package org.processmining.plugins.inductiveVisualMiner.mode;

import org.processmining.plugins.graphviz.colourMaps.ColourMapBlue;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data.AlignedLogVisualisationData;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data.AlignedLogVisualisationDataImplFrequencies;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObjectValues;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.sizeMaps.SizeMapLinear;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogInfo;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationParameters;

public class ModePaths extends Mode {

	private ProcessTreeVisualisationParameters visualisationParameters = new ProcessTreeVisualisationParameters();

	public ModePaths() {
		visualisationParameters.setShowFrequenciesOnModelEdges(true);
		visualisationParameters.setColourModelEdges(new ColourMapBlue());
		visualisationParameters.setModelEdgesWidth(new SizeMapLinear(1, 3));
		visualisationParameters.setShowFrequenciesOnMoveEdges(false);
		visualisationParameters.setShowLogMoves(false);
		visualisationParameters.setShowModelMoves(false);
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
		return "paths";
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

		return new AlignedLogVisualisationDataImplFrequencies(model, logInfo);
	}

	@Override
	public boolean isVisualisationDataUpdateWithTimeStep() {
		return false;
	}
}
