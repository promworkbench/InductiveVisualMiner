package org.processmining.plugins.inductiveVisualMiner.mode;

import org.processmining.plugins.graphviz.colourMaps.ColourMapBlue;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data.AlignedLogVisualisationData;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data.AlignedLogVisualisationDataImplFrequencies;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.sizeMaps.SizeMapLinear;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationParameters;

public class ModePaths extends Mode {

	public ProcessTreeVisualisationParameters visualisationParameters = new ProcessTreeVisualisationParameters();

	public ModePaths() {
		visualisationParameters.setShowFrequenciesOnModelEdges(true);
		visualisationParameters.setColourModelEdges(new ColourMapBlue());
		visualisationParameters.setModelEdgesWidth(new SizeMapLinear(1, 3));
		visualisationParameters.setShowFrequenciesOnMoveEdges(false);
		visualisationParameters.setShowLogMoves(false);
		visualisationParameters.setShowModelMoves(false);
	}

	public ProcessTreeVisualisationParameters getFinalVisualisationParameters(InductiveVisualMinerState state) {
		return visualisationParameters;
	}

	@Override
	public String toString() {
		return "paths";
	}

	public boolean isShowDeviations() {
		return false;
	}

	public boolean isShowPerformance() {
		return false;
	}

	public boolean isUpdateWithTimeStep(InductiveVisualMinerState state) {
		return false;
	}

	protected AlignedLogVisualisationData getFinalVisualisationData(InductiveVisualMinerState state) {
		return new AlignedLogVisualisationDataImplFrequencies(state.getModel(), state.getIvMLogInfoFiltered());
	}
}
