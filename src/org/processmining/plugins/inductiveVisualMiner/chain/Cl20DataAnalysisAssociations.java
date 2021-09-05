package org.processmining.plugins.inductiveVisualMiner.chain;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.plugins.InductiveMiner.AttributeClassifiers;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.traceattributes.CorrelationDensityPlot;
import org.processmining.statisticaltests.association.Associations;
import org.processmining.statisticaltests.association.AssociationsParametersAbstract;
import org.processmining.statisticaltests.association.AssociationsParametersDefault;
import org.processmining.statisticaltests.plugins.AssociationsPlugin;

public class Cl20DataAnalysisAssociations<C> extends DataChainLinkComputationAbstract<C> {

	public String getName() {
		return "Cl20 associations";
	}

	public String getStatusBusyMessage() {
		return "Computing associations..";
	}

	public IvMObject<?>[] createInputObjects() {
		return new IvMObject<?>[] { IvMObject.selected_associations_enabled, IvMObject.sorted_log,
				IvMObject.selected_classifier };
	}

	public IvMObject<?>[] createOutputObjects() {
		return new IvMObject<?>[] { IvMObject.data_analysis_associations };
	}

	public IvMObjectValues execute(C configuration, IvMObjectValues inputs, IvMCanceller canceller) throws Exception {
		if (inputs.get(IvMObject.selected_associations_enabled)) {
			XLog log = inputs.get(IvMObject.sorted_log);

			XEventClassifier classifier = AttributeClassifiers
					.constructClassifier(inputs.get(IvMObject.selected_classifier));

			AssociationsParametersAbstract parameters = new AssociationsParametersDefault();
			parameters.setClassifier(classifier);
			parameters.getCorrelationPlot().setSizeX1DPlot(CorrelationDensityPlot.sizeX1DPlot);
			parameters.getCorrelationPlot().setSizeX2DPlot(CorrelationDensityPlot.sizeX2DPlot);
			parameters.getCorrelationPlot().setSizeY1DPlot(CorrelationDensityPlot.sizeY1DPlot);
			parameters.getCorrelationPlot().setSizeY2DPlot(CorrelationDensityPlot.sizeY2DPlot);
			parameters.getCorrelationPlot().setAlias(CorrelationDensityPlot.alias);
			parameters.getCorrelationPlot().setMarginX(CorrelationDensityPlot.marginX);
			parameters.getCorrelationPlot().setMarginY(CorrelationDensityPlot.marginY);
			parameters.getCorrelationPlot().setColourMap(CorrelationDensityPlot.colourMap);
			parameters.getCorrelationPlot().setBackgroundFigure(CorrelationDensityPlot.backgroundFigure);
			parameters.getCorrelationPlot().setBackgroundPlot(CorrelationDensityPlot.backgroundPlot);
			Associations result = AssociationsPlugin.compute(log, parameters, canceller, null);

			return new IvMObjectValues().//
					s(IvMObject.data_analysis_associations, result);
		} else {
			return null;
		}
	}

}
