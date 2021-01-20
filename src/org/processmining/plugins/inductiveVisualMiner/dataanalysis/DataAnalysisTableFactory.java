package org.processmining.plugins.inductiveVisualMiner.dataanalysis;

import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;

public interface DataAnalysisTableFactory {

	public IvMObject<?>[] getInputObjects();

	public IvMObject<?>[] getOptionalObjects();

	public DataAnalysisTable create();

	/**
	 * 
	 * @return the name of the analysis. The first word must have a capital,
	 *         subsequent words must not.
	 */
	public String getAnalysisName();

	public String getExplanation();

	public boolean isSwitchable();

}
