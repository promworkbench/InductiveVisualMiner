package org.processmining.plugins.inductiveVisualMiner.dataanalysis.cohorts;

import org.processmining.cohortanalysis.cohort.Cohorts;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysisTable;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysisTableFactory;

public class CohortAnalysisTableFactory implements DataAnalysisTableFactory<Cohorts> {

	public static final String name = "Cohort analysis";
	public static final String explanation = "Study influence of trace attributes on process behaviour.\n"
			+ "Click: highlight cohort;\t"
			+ "shift+click: highlight other traces;\t"
			+ "ctrl+click: disable cohort highlighting.";

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