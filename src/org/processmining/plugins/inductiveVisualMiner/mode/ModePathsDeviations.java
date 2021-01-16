package org.processmining.plugins.inductiveVisualMiner.mode;

import java.awt.Color;

import org.processmining.plugins.graphviz.colourMaps.ColourMapFixed;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data.AlignedLogVisualisationData;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data.AlignedLogVisualisationDataImplFrequencies;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObjectValues;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogInfo;
import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceWrapper;
import org.processmining.plugins.inductiveVisualMiner.performance.QueueActivityLog;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationParameters;

import gnu.trove.map.TIntObjectMap;

public class ModePathsDeviations extends Mode {

	public ProcessTreeVisualisationParameters visualisationParameters = new ProcessTreeVisualisationParameters();

	public ModePathsDeviations() {
		visualisationParameters.setShowFrequenciesOnModelEdges(true);
		visualisationParameters.setShowFrequenciesOnMoveEdges(true);
		visualisationParameters.setColourModelEdges(new ColourMapFixed(new Color(153, 153, 255)));
		visualisationParameters.setColourMoves(new ColourMapFixed(new Color(255, 0, 0)));
	}

	@Override
	public IvMObject<?>[] inputsRequested() {
		return new IvMObject<?>[] {};
	}

	@Override
	public ProcessTreeVisualisationParameters getVisualisationParametersWithAlignments(IvMObjectValues inputs) {
		return visualisationParameters;
	}

	@Override
	public String toString() {
		return "paths and deviations";
	}

	@Override
	public boolean isShowDeviations() {
		return true;
	}

	@Override
	public boolean isUpdateWithTimeStep() {
		return false;
	}

	@Override
	public AlignedLogVisualisationData getVisualisationData(IvMModel model, IvMLogFiltered log, IvMLogInfo logInfo,
			TIntObjectMap<QueueActivityLog> queueActivityLogs, PerformanceWrapper performance) {
		return new AlignedLogVisualisationDataImplFrequencies(model, logInfo);
	}
}
