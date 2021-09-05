package org.processmining.plugins.inductiveVisualMiner.dataanalysis;

import java.util.List;

public interface DataAnalysisTab<O, C, P> {

	/**
	 * 
	 * @param dataAnalysesView
	 *            May be null.
	 * @return A new instance of a table to represent this data analysis tab.
	 */
	public DataAnalysisTable<O, C, P> createTable(DataAnalysesView<C, P> dataAnalysesView);

	public List<DataRowBlock<O, C, P>> createRowBlocks(DataAnalysisTable<O, C, P> table);

	public List<DataRowBlockComputer<O, C, P>> createRowBlockComputers();

	public String getAnalysisName();

	public String getExplanation();

}