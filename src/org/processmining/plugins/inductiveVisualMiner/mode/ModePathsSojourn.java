package org.processmining.plugins.inductiveVisualMiner.mode;

import java.awt.Color;

import org.processmining.plugins.graphviz.colourMaps.ColourMapFixed;
import org.processmining.plugins.graphviz.colourMaps.ColourMapRed;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data.AlignedLogVisualisationData;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data.AlignedLogVisualisationDataImplPlaceholder;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data.AlignedLogVisualisationDataImplSojourn;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObjectValues;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.sizeMaps.SizeMapFixed;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogInfo;
import org.processmining.plugins.inductiveVisualMiner.performance.Performance;
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

	@Override
	public IvMObject<?>[] createOptionalObjects() {
		return new IvMObject<?>[] { IvMObject.performance };
	}

	@Override
	public ProcessTreeVisualisationParameters getVisualisationParametersWithAlignments(IvMObjectValues inputs) {
		if (!inputs.has(IvMObject.performance)) {
			return visualisationParametersBeforeQueues;
		}
		return visualisationParameters;
	}

	@Override
	public String toString() {
		return "paths and sojourn times";
	}

	@Override
	public boolean isShowDeviations() {
		return false;
	}

	@Override
	protected IvMObject<?>[] createVisualisationDataOptionalObjects() {
		return new IvMObject<?>[] { IvMObject.model, IvMObject.aligned_log_info_filtered, IvMObject.performance };
	}

	@Override
	public AlignedLogVisualisationData getVisualisationData(IvMObjectValues inputs) {
		IvMModel model = inputs.get(IvMObject.model);
		IvMLogInfo logInfo = inputs.get(IvMObject.aligned_log_info_filtered);
		if (inputs.has(IvMObject.performance)) {
			Performance performance = inputs.get(IvMObject.performance);

			return new AlignedLogVisualisationDataImplSojourn(model, performance, logInfo);
		} else {
			return new AlignedLogVisualisationDataImplPlaceholder(model, logInfo);
		}
	}

	@Override
	public boolean isVisualisationDataUpdateWithTimeStep() {
		return false;
	}
}
