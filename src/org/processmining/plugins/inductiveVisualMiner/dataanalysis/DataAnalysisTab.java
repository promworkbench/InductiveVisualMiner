package org.processmining.plugins.inductiveVisualMiner.dataanalysis;

import java.util.List;

import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;

public interface DataAnalysisTab<O, C, P> {

	/**
	 * 
	 * @param dataAnalysesView
	 *            May be null.
	 * @param decorator
	 * @return A new instance of a table to represent this data analysis tab.
	 */
	public DataAnalysisTable<O, C, P> createTable(DataAnalysesView<C, P> dataAnalysesView, IvMDecoratorI decorator);

	/**
	 * RowBlocks are groups of rows that are changed together.
	 * 
	 * @param table
	 * @return
	 */
	public List<DataRowBlock<O, C, P>> createRowBlocks(DataAnalysisTable<O, C, P> table);

	/**
	 * RowBlockComputers are chainlinks that compute their RowBlocks themselves.
	 * 
	 * @return
	 */
	public List<DataRowBlockComputer<O, C, P>> createRowBlockComputers();

	public String getAnalysisName();

	public String getExplanation();

}