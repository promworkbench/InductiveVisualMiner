package org.processmining.plugins.inductiveVisualMiner.dataanalysis.logattributes;

import org.deckfour.xes.model.XLog;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysisTable;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysisTableFactory;

public class LogAttributeAnalysisFactory implements DataAnalysisTableFactory<XLog> {

	public static final String name = "Log attributes";
	public static final String explanation = "Attributes at the log level.";

	public DataAnalysisTable<XLog> create() {
		return new LogAttributeAnalysisTable();
	}

	public String getAnalysisName() {
		return name;
	}

	public String getExplanation() {
		return explanation;
	}

}