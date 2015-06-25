package org.processmining.plugins.inductiveVisualMiner.colouringmode;

import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.AlignedLogVisualisationParameters;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.sizeMaps.SizeMapFixed;

public abstract class ColouringMode {
	
	protected abstract AlignedLogVisualisationParameters getFinalVisualisationParameters();
	public abstract boolean isShowDeviations();
	public abstract boolean isShowQueueLengths();
	public abstract boolean isUpdateWithTimeStep(InductiveVisualMinerState state);
	
	private static AlignedLogVisualisationParameters withoutAlignment = new AlignedLogVisualisationParameters();
	
	public ColouringMode() {
		withoutAlignment.setColourModelEdges(null);
		withoutAlignment.setShowFrequenciesOnModelEdges(false);
		withoutAlignment.setShowFrequenciesOnNodes(false);
		withoutAlignment.setModelEdgesWidth(new SizeMapFixed(1));
	}
	
	public AlignedLogVisualisationParameters getVisualisationParameters(InductiveVisualMinerState state) {
		if (!state.isAlignmentReady()) {
			return withoutAlignment;
		}
		if (!state.isQueueLengthsReady()) {
			return getVisualisationParametersBeforeQueues();
		}
		return getFinalVisualisationParameters();
	}
	
	protected AlignedLogVisualisationParameters getVisualisationParametersBeforeQueues() {
		return getFinalVisualisationParameters();
	}
}
