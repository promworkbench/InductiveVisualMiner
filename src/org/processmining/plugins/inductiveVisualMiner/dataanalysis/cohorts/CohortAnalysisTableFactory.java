package org.processmining.plugins.inductiveVisualMiner.dataanalysis.cohorts;

import org.processmining.cohortanalysis.cohort.Cohorts;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysisTable;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysisTableFactory;

public class CohortAnalysisTableFactory implements DataAnalysisTableFactory<Cohorts> {

	public static final String name = "Cohort analysis";
	public static final String explanation = "Cohort analysis quantifies the influence of trace attributes on stochastic process behaviour: "
			+ "1 means that the process is completely defined by the attribute, 0 means that the influence is indistinguishable from randomness.\n"
			+ "Click on a cohort to highlight it; shift+click to highlight other traces; ctrl+click to un-highlight it.";

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