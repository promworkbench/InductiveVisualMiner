package org.processmining.plugins.inductiveVisualMiner.mode;

import java.awt.Color;

import org.processmining.plugins.graphviz.colourMaps.ColourMapFixed;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data.AlignedLogVisualisationData;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data.AlignedLogVisualisationDataImplEmpty;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.sizeMaps.SizeMapFixed;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationParameters;

public abstract class Mode {
	
	protected abstract ProcessTreeVisualisationParameters getFinalVisualisationParameters(InductiveVisualMinerState state);
	protected abstract AlignedLogVisualisationData getFinalVisualisationData(InductiveVisualMinerState state);
	
	public abstract boolean isShowDeviations();
	public abstract boolean isShowPerformance();
	public abstract boolean isUpdateWithTimeStep(InductiveVisualMinerState state);
	
	private static ProcessTreeVisualisationParameters withoutAlignment = new ProcessTreeVisualisationParameters();
	
	public Mode() {
		withoutAlignment.setColourModelEdges(null);
		withoutAlignment.setShowFrequenciesOnModelEdges(false);
		withoutAlignment.setShowFrequenciesOnMoveEdges(false);
		withoutAlignment.setShowFrequenciesOnNodes(false);
		withoutAlignment.setModelEdgesWidth(new SizeMapFixed(1));
		withoutAlignment.setShowLogMoves(false);
		withoutAlignment.setShowModelMoves(false);
		withoutAlignment.setColourNodes(new ColourMapFixed(Color.white));
	}
	
	public ProcessTreeVisualisationParameters getVisualisationParameters(InductiveVisualMinerState state) {
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
