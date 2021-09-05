package org.processmining.plugins.inductiveVisualMiner.chain;

import org.deckfour.xes.model.XLog;
import org.processmining.cohortanalysis.cohort.Cohorts;
import org.processmining.plugins.InductiveMiner.AttributeClassifiers;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.cohorts.CohortAnalysis;
import org.processmining.plugins.inductiveVisualMiner.performance.XEventPerformanceClassifier;
import org.processmining.plugins.inductiveminer2.attributes.AttributesInfo;

public class Cl18DataAnalysisCohort<C> extends DataChainLinkComputationAbstract<C> {

	@Override
	public String getName() {
		return "cohort analysis";
	}

	@Override
	public String getStatusBusyMessage() {
		return "Performing cohort analysis..";
	}

	@Override
	public IvMObject<?>[] createInputObjects() {
		return new IvMObject<?>[] { IvMObject.attributes_info, IvMObject.sorted_log, IvMObject.selected_classifier,
				IvMObject.selected_cohort_analysis_enabled };
	}

	@Override
	public IvMObject<?>[] createOutputObjects() {
		return new IvMObject<?>[] { IvMObject.data_analysis_cohort };
	}

	public IvMObjectValues execute(C configuration, IvMObjectValues inputs, IvMCanceller canceller) throws Exception {
		if (inputs.get(IvMObject.selected_cohort_analysis_enabled)) {
			AttributesInfo attributesInfo = inputs.get(IvMObject.attributes_info);
			XLog log = inputs.get(IvMObject.sorted_log);
			XEventPerformanceClassifier classifier = new XEventPerformanceClassifier(
					AttributeClassifiers.constructClassifier(inputs.get(IvMObject.selected_classifier)));

			Cohorts cohorts = CohortAnalysis.compute(attributesInfo, log, classifier, canceller);

			return new IvMObjectValues().//
					s(IvMObject.data_analysis_cohort, cohorts);
		} else {
			return null;
		}
	}

}