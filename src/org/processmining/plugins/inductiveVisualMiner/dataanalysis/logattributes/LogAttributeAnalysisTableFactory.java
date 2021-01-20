package org.processmining.plugins.inductiveVisualMiner.dataanalysis.logattributes;

import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysisTable;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysisTableFactoryAbstract;

public class LogAttributeAnalysisTableFactory extends DataAnalysisTableFactoryAbstract {

	public static final String name = "Log attributes";
	public static final String explanation = "Attributes at the log level.";

	@Override
	public DataAnalysisTable create() {
		return new LogAttributeAnalysisTable();
	}

	@Override
	public String getAnalysisName() {
		return name;
	}

	@Override
	public String getExplanation() {
		return explanation;
	}

	@Override
	public boolean isSwitchable() {
		return false;
	}

	@Override
	public IvMObject<?>[] createOptionalObjects() {
		return new IvMObject<?>[] { IvMObject.data_analysis_log_virtual_attributes };
	}

	@Override
	public IvMObject<?>[] createInputObjects() {
		return new IvMObject<?>[] { IvMObject.data_analysis_log };
	}

}