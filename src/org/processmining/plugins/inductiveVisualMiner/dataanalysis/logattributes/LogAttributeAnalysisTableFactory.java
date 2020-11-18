package org.processmining.plugins.inductiveVisualMiner.dataanalysis.logattributes;

import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysisTable;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysisTableFactory;

public class LogAttributeAnalysisTableFactory implements DataAnalysisTableFactory {

	public static final String name = "Log attributes";
	public static final String explanation = "Attributes at the log level.";

	public DataAnalysisTable create() {
		return new LogAttributeAnalysisTable();
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