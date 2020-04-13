package org.processmining.plugins.inductiveVisualMiner.dataanalysis.cohorts;

import org.processmining.cohortanalysis.cohort.Cohorts;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysisTable;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysisTableFactory;

public class CohortAnalysisTableFactory implements DataAnalysisTableFactory<Cohorts> {

	public static final String name = "Cohort analysis";
	public static final String explanation = "Cohort analysis identifies differences between groups of traces. "
			+ "It looks for the trace attributes with the largest influence on stochastic process behaviour: "
			+ "1 means that the process is completely defined by the attribute, 0 means that the influence is indistinguishable from randomness.";

	public DataAnalysisTable<Cohorts> create() {
		return new CohortAnalysisTable();
	}

	public String getAnalysisName() {
		return name;
	}

	public String getExplanation() {
		return explanation;
	}

}