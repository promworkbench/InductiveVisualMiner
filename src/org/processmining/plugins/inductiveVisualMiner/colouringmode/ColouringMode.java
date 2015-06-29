package org.processmining.plugins.inductiveVisualMiner.colouringmode;

import java.awt.Color;

import org.processmining.plugins.graphviz.colourMaps.ColourMapFixed;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.AlignedLogVisualisationData;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.AlignedLogVisualisationDataImplEmpty;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.AlignedLogVisualisationParameters;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.sizeMaps.SizeMapFixed;

public abstract class ColouringMode {
	
	protected abstract AlignedLogVisualisationParameters getFinalVisualisationParameters(InductiveVisualMinerState state);
	protected abstract AlignedLogVisualisationData getFinalVisualisationData(InductiveVisualMinerState state);
	
	public abstract boolean isShowDeviations();
	public abstract boolean isShowPerformance();
	public abstract boolean isUpdateWithTimeStep(InductiveVisualMinerState state);
	
	private static AlignedLogVisualisationParameters withoutAlignment = new AlignedLogVisualisationParameters();
	
	public ColouringMode() {
		withoutAlignment.setColourModelEdges(null);
		withoutAlignment.setShowFrequenciesOnModelEdges(false);
		withoutAlignment.setShowFrequenciesOnMoveEdges(false);
		withoutAlignment.setShowFrequenciesOnNodes(false);
		withoutAlignment.setModelEdgesWidth(new SizeMapFixed(1));
		withoutAlignment.setShowLogMoves(false);
		withoutAlignment.setShowModelMoves(false);
		withoutAlignment.setColourNodes(new ColourMapFixed(Color.white));
	}
	
	public AlignedLogVisualisationParameters getVisualisationParameters(InductiveVisualMinerState state) {
		if (!state.isAlignmentReady()) {
			return withoutAlignment;
		}
		return getFinalVisualisationParameters(state);
	}
	
	public AlignedLogVisualisationData getVisualisationData(InductiveVisualMinerState state) {
		if (!state.isAlignmentReady()) {
			return new AlignedLogVisualisationDataImplEmpty();
		}
		return getFinalVisualisationData(state);
	}
}
