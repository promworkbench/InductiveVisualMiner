package org.processmining.plugins.inductiveVisualMiner.dataanalysis.traceattributes;

import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysisTable;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysisTableFactory;

public class TraceAttributeAnalysisTableFactory implements DataAnalysisTableFactory<TraceAttributeAnalysis> {

	public static final String name = "Trace attributes";
	public static final String explanation = "Trace attribute analysis shows descriptive statistics of trace attributes, and correlates them with fitness. If a highlighting filter is active, it will show this for both the highlighted and the non-highlighted traces.";

	public DataAnalysisTable<TraceAttributeAnalysis> create() {
		return new TraceAttributeAnalysisTable();
	}

	public String getAnalysisName() {
		return name;
	}

	public String getExplanation() {
		return explanation;
	}

}
