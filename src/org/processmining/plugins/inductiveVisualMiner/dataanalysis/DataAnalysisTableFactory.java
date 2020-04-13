package org.processmining.plugins.inductiveVisualMiner.dataanalysis;

public interface DataAnalysisTableFactory<D> {

	public DataAnalysisTable<D> create();

	/**
	 * 
	 * @return the name of the analysis. The first word must have a capital,
	 *         subsequent words must not.
	 */
	public String getAnalysisName();
	
	public String getExplanation();

}
