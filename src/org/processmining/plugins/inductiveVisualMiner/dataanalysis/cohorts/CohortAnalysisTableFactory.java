package org.processmining.plugins.inductiveVisualMiner.dataanalysis.cohorts;

import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysisTable;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysisTableFactoryAbstract;

public class CohortAnalysisTableFactory extends DataAnalysisTableFactoryAbstract {

	public static final String name = "Cohort analysis";
	public static final String explanation = "Study influence of trace attributes on process behaviour.\n"
			+ "Click: highlight cohort;\t" + "shift+click: highlight other traces;\t"
			+ "ctrl+click: disable cohort highlighting.";

	public DataAnalysisTable create() {
		return new CohortAnalysisTable();
	}

	public String getAnalysisName() {
		return name;
	}

	public String getExplanation() {
		return explanation;
	}

	public boolean isSwitchable() {
		return true;
	}

	public IvMObject<?>[] createInputObjects() {
		return new IvMObject<?>[] { IvMObject.data_analysis_cohort };
	}
}