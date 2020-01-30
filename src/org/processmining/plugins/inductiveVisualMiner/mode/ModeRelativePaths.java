package org.processmining.plugins.inductiveVisualMiner.mode;

import org.processmining.plugins.graphviz.colourMaps.ColourMapLightGreen;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data.AlignedLogVisualisationData;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data.AlignedLogVisualisationDataImplRelativeFrequencies;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.sizeMaps.SizeMapLinear;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogInfo;
import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceWrapper;
import org.processmining.plugins.inductiveVisualMiner.performance.QueueActivityLog;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationParameters;

import gnu.trove.map.TIntObjectMap;

public class ModeRelativePaths extends Mode {

	public ProcessTreeVisualisationParameters visualisationParameters = new ProcessTreeVisualisationParameters();

	public ModeRelativePaths() {
		visualisationParameters.setShowFrequenciesOnModelEdges(true);
		visualisationParameters.setColourModelEdges(new ColourMapLightGreen());
		visualisationParameters.setModelEdgesWidth(new SizeMapLinear(1, 3));
		visualisationParameters.setShowFrequenciesOnMoveEdges(false);
		visualisationParameters.setShowLogMoves(false);
		visualisationParameters.setShowModelMoves(false);

		visualisationParameters.setColourNodes(new ColourMapLightGreen());
	}

	public ProcessTreeVisualisationParameters getFinalVisualisationParameters(InductiveVisualMinerState state) {
		return visualisationParameters;
	}

	@Override
	public String toString() {
		return "relative paths";
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

	@Override
	public AlignedLogVisualisationData getVisualisationData(IvMModel model, IvMLogFiltered log, IvMLogInfo logInfo,
			TIntObjectMap<QueueActivityLog> queueActivityLogs, PerformanceWrapper performance) {
		return new AlignedLogVisualisationDataImplRelativeFrequencies(model, logInfo);
	}
}
