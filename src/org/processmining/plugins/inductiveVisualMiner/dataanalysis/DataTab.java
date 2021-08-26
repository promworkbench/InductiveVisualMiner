package org.processmining.plugins.inductiveVisualMiner.dataanalysis;

import java.util.List;

public interface DataTab<C, P> {

	/**
	 * 
	 * @param dataAnalysesView
	 *            May be null.
	 * @return
	 */
	public DataTable<C, P> createTable(DataAnalysesView<C, P> dataAnalysesView);

	public List<DataRowBlock<C, P>> createRowBlocks(DataTable<C, P> table);

	public List<DataRowBlockComputer<C, P>> createRowBlockComputers();

	public String getAnalysisName();

	public String getExplanation();

	public boolean isSwitchable();

}