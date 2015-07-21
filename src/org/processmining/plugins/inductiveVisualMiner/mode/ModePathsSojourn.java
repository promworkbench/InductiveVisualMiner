package org.processmining.plugins.inductiveVisualMiner.mode;

import java.awt.Color;

import org.processmining.plugins.graphviz.colourMaps.ColourMapFixed;
import org.processmining.plugins.graphviz.colourMaps.ColourMapRed;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data.AlignedLogVisualisationData;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data.AlignedLogVisualisationDataImplFrequencies;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data.AlignedLogVisualisationDataImplSojourn;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.sizeMaps.SizeMapFixed;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationParameters;

public class ModePathsSojourn extends Mode {

	
	public ProcessTreeVisualisationParameters visualisationParametersBeforeQueues = new ProcessTreeVisualisationParameters();
	public ProcessTreeVisualisationParameters visualisationParameters = new ProcessTreeVisualisationParameters();

	public ModePathsSojourn() {
		visualisationParameters.setShowFrequenciesOnModelEdges(true);
		visualisationParameters.setColourModelEdges(new ColourMapFixed(new Color(187, 187, 255)));
		visualisationParameters.setShowLogMoves(false);
		visualisationParameters.setShowModelMoves(false);
		visualisationParameters.setColourNodes(new ColourMapRed());
		visualisationParameters.setModelEdgesWidth(new SizeMapFixed(1));
		
		visualisationParametersBeforeQueues.setShowFrequenciesOnModelEdges(true);
		visualisationParametersBeforeQueues.setColourModelEdges(new ColourMapFixed(new Color(187, 187, 255)));
		visualisationParametersBeforeQueues.setShowLogMoves(false);
		visualisationParametersBeforeQueues.setShowModelMoves(false);
		visualisationParametersBeforeQueues.setColourNodes(new ColourMapFixed(Color.white));
	}

	public ProcessTreeVisualisationParameters getFinalVisualisationParameters(InductiveVisualMinerState state) {
		if (!state.isPerformanceReady()) {
			return visualisationParametersBeforeQueues;
		}
		return visualisationParameters;
	}

	@Override
	public String toString() {
		return "paths and sojourn times";
	}

	public boolean isShowDeviations() {
		return false;
	}

	public boolean isShowPerformance() {
		return true;
	}

	public boolean isUpdateWithTimeStep(InductiveVisualMinerState state) {
		return false;
	}
	
	protected AlignedLogVisualisationData getFinalVisualisationData(InductiveVisualMinerState state) {
		if (!state.isPerformanceReady()) {
			return new AlignedLogVisualisationDataImplFrequencies(state.getTree(), state.getIvMLogInfoFiltered());
		}
		return new AlignedLogVisualisationDataImplSojourn(state.getTree(), state.getPerformance(),
				state.getIvMLogInfoFiltered());
	}
}
