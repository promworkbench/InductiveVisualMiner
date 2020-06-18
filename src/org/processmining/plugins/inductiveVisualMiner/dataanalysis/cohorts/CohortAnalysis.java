package org.processmining.plugins.inductiveVisualMiner.dataanalysis.cohorts;

import java.util.concurrent.ExecutionException;

import org.deckfour.xes.model.XLog;
import org.processmining.cohortanalysis.cohort.Cohorts;
import org.processmining.cohortanalysis.parameters.CohortAnalysisParametersAbstract;
import org.processmining.cohortanalysis.parameters.CohortAnalysisParametersDefault;
import org.processmining.cohortanalysis.plugins.CohortAnalysisPlugin;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.plugins.inductiveVisualMiner.performance.XEventPerformanceClassifier;
import org.processmining.plugins.inductiveminer2.attributes.AttributesInfo;

public class CohortAnalysis {

	public static Cohorts compute(AttributesInfo attributes, XLog log, XEventPerformanceClassifier classifier,
			ProMCanceller canceller) throws ExecutionException {
		CohortAnalysisParametersAbstract parameters = new CohortAnalysisParametersDefault();
		parameters.setClassifier(classifier);
		parameters.setMaxFeatureSetSize(1);
		parameters.setDiversifyThreshold(1); //disable diversification
		parameters.setNumberOfThreads(Math.max(1, Runtime.getRuntime().availableProcessors() / 2)); //keep half of the cores available for other tasks. A bit crude, but we don't want to interfere with the alignments too much.
		parameters.setDebug(false);
		return CohortAnalysisPlugin.measure(log, parameters, canceller);
	}

}