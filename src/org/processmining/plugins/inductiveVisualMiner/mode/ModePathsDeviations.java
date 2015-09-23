package org.processmining.plugins.inductiveVisualMiner.mode;

import java.awt.Color;

import org.processmining.plugins.graphviz.colourMaps.ColourMapFixed;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data.AlignedLogVisualisationData;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data.AlignedLogVisualisationDataImplFrequencies;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationParameters;

public class ModePathsDeviations extends Mode {

	public ProcessTreeVisualisationParameters visualisationParameters = new ProcessTreeVisualisationParameters();

	public ModePathsDeviations() {
		visualisationParameters.setShowFrequenciesOnModelEdges(true);
		visualisationParameters.setShowFrequenciesOnMoveEdges(true);
		//visualisationParameters.setColourModelEdges(new ColourMapFixed(new Color(153, 153, 255)));
		visualisationParameters.setColourModelEdges(new ColourMapFixed(Color.black));
		//visualisationParameters.setColourMoves(new ColourMapFixed(new Color(255, 0, 0)));
		visualisationParameters.setColourMoves(new ColourMapFixed(Color.black));
	}

	public ProcessTreeVisualisationParameters getFinalVisualisationParameters(InductiveVisualMinerState state) {
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
		return new AlignedLogVisualisationDataImplFrequencies(state.getTree(), state.getIvMLogInfoFiltered());
	}
}
