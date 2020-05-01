package org.processmining.plugins.inductiveVisualMiner.dataanalysis.eventattributes;

import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysisTable;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysisTableFactory;

public class EventAttributeAnalysisTableFactory implements DataAnalysisTableFactory<EventAttributeAnalysis> {

	public static final String name = "Event attributes";
	public static final String explanation = "Event attribute analysis shows descriptive statistics of event attributes. If a highlighting filter is active, it will show this for both the highlighted and the non-highlighted traces.";

	public DataAnalysisTable<EventAttributeAnalysis> create() {
		return new EventAttributeAnalysisTable();
	}

	public String getAnalysisName() {
		return name;
	}

	public String getExplanation() {
		return explanation;
	}

}
