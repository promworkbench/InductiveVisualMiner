package org.processmining.plugins.inductiveVisualMiner.mode;

import java.awt.Color;

import org.processmining.plugins.graphviz.colourMaps.ColourMapFixed;
import org.processmining.plugins.graphviz.colourMaps.ColourMapPurple;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data.AlignedLogVisualisationData;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data.AlignedLogVisualisationDataImplCost;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data.AlignedLogVisualisationDataImplPlaceholder;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObjectValues;
import org.processmining.plugins.inductiveVisualMiner.cost.CostModel;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.sizeMaps.SizeMapFixed;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogInfo;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationParameters;

public class ModeCost extends Mode {

	public ProcessTreeVisualisationParameters visualisationParametersBeforeCost = new ProcessTreeVisualisationParameters();
	public ProcessTreeVisualisationParameters visualisationParameters = new ProcessTreeVisualisationParameters();

	public ModeCost() {
		visualisationParameters.setShowFrequenciesOnModelEdges(true);
		visualisationParameters.setColourModelEdges(new ColourMapFixed(new Color(187, 187, 255)));
		visualisationParameters.setShowLogMoves(false);
		visualisationParameters.setShowModelMoves(false);
		visualisationParameters.setColourNodes(new ColourMapPurple());
		visualisationParameters.setModelEdgesWidth(new SizeMapFixed(1));

		visualisationParametersBeforeCost.setShowFrequenciesOnModelEdges(true);
		visualisationParametersBeforeCost.setColourModelEdges(new ColourMapFixed(new Color(187, 187, 255)));
		visualisationParametersBeforeCost.setShowLogMoves(false);
		visualisationParametersBeforeCost.setShowModelMoves(false);
		visualisationParametersBeforeCost.setColourNodes(new ColourMapFixed(Color.white));
	}

	public IvMObject<?>[] createOptionalObjects() {
		return new IvMObject<?>[] { IvMObject.cost_model };
	}

	public ProcessTreeVisualisationParameters getVisualisationParametersWithAlignments(IvMObjectValues inputs) {
		if (!inputs.has(IvMObject.cost_model)) {
			return visualisationParametersBeforeCost;
		}
		return visualisationParameters;
	}

	@Override
	public String toString() {
		return "paths and cost";
	}

	@Override
	public boolean isShowDeviations() {
		return false;
	}

	@Override
	protected IvMObject<?>[] createVisualisationDataOptionalObjects() {
		return new IvMObject<?>[] { IvMObject.model, IvMObject.aligned_log_info_filtered, IvMObject.cost_model };
	}

	@Override
	public AlignedLogVisualisationData getVisualisationData(IvMObjectValues inputs) {
		IvMModel model = inputs.get(IvMObject.model);
		IvMLogInfo logInfo = inputs.get(IvMObject.aligned_log_info_filtered);

		if (inputs.has(IvMObject.cost_model)) {
			CostModel costModel = inputs.get(IvMObject.cost_model);

			return new AlignedLogVisualisationDataImplCost(model, costModel, logInfo);
		} else {
			return new AlignedLogVisualisationDataImplPlaceholder(model, logInfo);
		}
	}

	@Override
	public boolean isVisualisationDataUpdateWithTimeStep() {
		return false;
	}

}
