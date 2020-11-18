package org.processmining.plugins.inductiveVisualMiner.dataanalysis.traceattributes;

import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysisTable;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysisTableFactory;

public class TraceAttributeAnalysisTableFactory implements DataAnalysisTableFactory {

	public static final String name = "Trace attributes";
	public static final String explanation = "Attributes at the trace level.\nIf traces are highlighted, attributes will be shown for highlighted and non-highlighted traces.";

	public DataAnalysisTable create() {
		return new TraceAttributeAnalysisTable();
	}

	public String getAnalysisName() {
		return name;
	}

	public String getExplanation() {
		return explanation;
	}

	public boolean isSwitchable() {
		return false;
	}

}
