package org.processmining.plugins.inductiveVisualMiner.colouringmode;

import java.awt.Color;

import org.processmining.plugins.graphviz.colourMaps.ColourMapFixed;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.AlignedLogVisualisationData;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.AlignedLogVisualisationDataImplFrequencies;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.AlignedLogVisualisationParameters;

public class ColouringModePathsDeviations extends ColouringMode {

	public AlignedLogVisualisationParameters visualisationParameters = new AlignedLogVisualisationParameters();

	public ColouringModePathsDeviations() {
		visualisationParameters.setShowFrequenciesOnModelEdges(true);
		visualisationParameters.setShowFrequenciesOnMoveEdges(true);
		visualisationParameters.setColourModelEdges(new ColourMapFixed(new Color(153, 153, 255)));
		visualisationParameters.setColourMoves(new ColourMapFixed(new Color(255, 0, 0)));
	}

	public AlignedLogVisualisationParameters getFinalVisualisationParameters(InductiveVisualMinerState state) {
		return visualisationParameters;
	}

	@Override
	public String toString() {
		return "paths and deviations";
	}

	public boolean isShowDeviations() {
		return true;
	}

	public boolean isShowPerformance() {
		return false;
	}
	
	public boolean isUpdateWithTimeStep(InductiveVisualMinerState state) {
		return false;
	}

	protected AlignedLogVisualisationData getFinalVisualisationData(InductiveVisualMinerState state) {
		return new AlignedLogVisualisationDataImplFrequencies(state.getTree(), state.getAlignedFilteredLogInfo());
	}
}
