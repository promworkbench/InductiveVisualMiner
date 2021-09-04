package org.processmining.plugins.inductiveVisualMiner.dataanalysis;

import java.util.List;

public interface DataAnalysisTab<O, C, P> {

	/**
	 * 
	 * @param dataAnalysesView
	 *            May be null.
	 * @return
	 */
	public DataAnalysisTable<O, C, P> createTable(DataAnalysesView<C, P> dataAnalysesView);

	public List<DataRowBlock<O, C, P>> createRowBlocks(DataAnalysisTable<O, C, P> table);

	public List<DataRowBlockComputer<O, C, P>> createRowBlockComputers();

	public String getAnalysisName();

	public String getExplanation();

	public boolean isSwitchable();

}