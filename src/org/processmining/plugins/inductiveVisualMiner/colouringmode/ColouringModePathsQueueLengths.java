package org.processmining.plugins.inductiveVisualMiner.colouringmode;

import java.awt.Color;

import org.processmining.plugins.graphviz.colourMaps.ColourMapFixed;
import org.processmining.plugins.graphviz.colourMaps.ColourMapRed;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.AlignedLogVisualisationParameters;

public class ColouringModePathsQueueLengths extends ColouringMode {

	
	public AlignedLogVisualisationParameters visualisationParametersBeforeQueues = new AlignedLogVisualisationParameters();
	public AlignedLogVisualisationParameters visualisationParameters = new AlignedLogVisualisationParameters();

	public ColouringModePathsQueueLengths() {
		visualisationParameters.setShowFrequenciesOnModelEdges(true);
		visualisationParameters.setColourModelEdges(new ColourMapFixed(new Color(187, 187, 255)));
		visualisationParameters.setShowLogMoves(false);
		visualisationParameters.setShowModelMoves(false);
		visualisationParameters.setColourNodes(new ColourMapRed());
		
		visualisationParametersBeforeQueues.setShowFrequenciesOnModelEdges(true);
		visualisationParametersBeforeQueues.setColourModelEdges(new ColourMapFixed(new Color(187, 187, 255)));
		visualisationParametersBeforeQueues.setShowLogMoves(false);
		visualisationParametersBeforeQueues.setShowModelMoves(false);
		visualisationParametersBeforeQueues.setColourNodes(new ColourMapFixed(Color.white));
	}

	public AlignedLogVisualisationParameters getFinalVisualisationParameters() {
		return visualisationParameters;
	}
	
	@Override
	protected AlignedLogVisualisationParameters getVisualisationParametersBeforeQueues() {
		return visualisationParametersBeforeQueues;
	}

	@Override
	public String toString() {
		return "paths and queue lengths";
	}

	public boolean isShowDeviations() {
		return false;
	}

	public boolean isShowQueueLengths() {
		return true;
	}

	public boolean isUpdateWithTimeStep(InductiveVisualMinerState state) {
		return state.isQueueLengthsReady();
	}
}
