package org.processmining.plugins.inductiveVisualMiner.mode;

import java.awt.Color;

import org.processmining.plugins.graphviz.colourMaps.ColourMapFixed;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data.AlignedLogVisualisationData;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.sizeMaps.SizeMapFixed;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogInfo;
import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceWrapper;
import org.processmining.plugins.inductiveVisualMiner.performance.QueueActivityLog;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationParameters;

import gnu.trove.map.TIntObjectMap;

public abstract class Mode {

	protected abstract ProcessTreeVisualisationParameters getFinalVisualisationParameters(
			InductiveVisualMinerState state);

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

	/**
	 * Perform the computations necessary to visualise something on the model.
	 * Will be called asynchronously, and the result must not be cached.
	 * 
	 * @param model
	 * @param log
	 * @param logInfo
	 * @param queueActivityLogs
	 *            Gives access to activity instance executions and their
	 *            events/moves
	 * @param performance
	 * @return
	 */
	public abstract AlignedLogVisualisationData getVisualisationData(IvMModel model, IvMLogFiltered log,
			IvMLogInfo logInfo, TIntObjectMap<QueueActivityLog> queueActivityLogs, PerformanceWrapper performance);
}
