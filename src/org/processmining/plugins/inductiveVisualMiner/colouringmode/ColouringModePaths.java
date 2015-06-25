package org.processmining.plugins.inductiveVisualMiner.colouringmode;

import org.processmining.plugins.graphviz.colourMaps.ColourMapBlue;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.AlignedLogVisualisationParameters;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.sizeMaps.SizeMapLinear;

public class ColouringModePaths extends ColouringMode {

	public AlignedLogVisualisationParameters visualisationParameters = new AlignedLogVisualisationParameters();

	public ColouringModePaths() {
		visualisationParameters.setShowFrequenciesOnModelEdges(true);
		visualisationParameters.setColourModelEdges(new ColourMapBlue());
		visualisationParameters.setModelEdgesWidth(new SizeMapLinear(1, 3));
		visualisationParameters.setShowFrequenciesOnMoveEdges(false);
		visualisationParameters.setShowLogMoves(false);
		visualisationParameters.setShowModelMoves(false);
	}

	public AlignedLogVisualisationParameters getFinalVisualisationParameters() {
		return visualisationParameters;
	}

	@Override
	public String toString() {
		return "paths";
	}

	public boolean isShowDeviations() {
		return false;
	}

	public boolean isShowQueueLengths() {
		return false;
	}

	public boolean isUpdateWithTimeStep(InductiveVisualMinerState state) {
		return false;
	}
}
