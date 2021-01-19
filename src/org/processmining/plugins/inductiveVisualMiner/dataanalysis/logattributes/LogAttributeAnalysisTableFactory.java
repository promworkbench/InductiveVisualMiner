package org.processmining.plugins.inductiveVisualMiner.dataanalysis.logattributes;

import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysisTable;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysisTableFactoryAbstract;

public class LogAttributeAnalysisTableFactory extends DataAnalysisTableFactoryAbstract {

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

	public IvMObject<?>[] createTriggerObjects() {
		return new IvMObject<?>[] { IvMObject.data_analysis_log_virtual_attributes };
	}

	public IvMObject<?>[] createInputObjects() {
		return new IvMObject<?>[] { IvMObject.data_analysis_log };
	}

}