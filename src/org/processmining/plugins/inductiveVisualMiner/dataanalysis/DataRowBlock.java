package org.processmining.plugins.inductiveVisualMiner.dataanalysis;

import org.processmining.plugins.inductiveVisualMiner.chain.DataChainLinkGui;

/**
 * A DataRowBlock visualises a set of dataRows. If the dataRows need to be
 * computed, use a DataRowComputer instead.
 * 
 * Notice that the methods need to be consistent and any update should be
 * performed on the gui thread.
 * 
 * @author sander
 *
 * @param <O>
 *            row payload
 * @param <C>
 * @param <P>
 */
public interface DataRowBlock<O, C, P> extends DataChainLinkGui<C, P> {

	public void setTable(DataAnalysisTable<O, C, P> table);

	public boolean showsSomething();

	public int getNumberOfRows();

	public DataRow<O> getRow(int row);

	public int getMaxNumberOfNames();

	public int getMaxNumberOfValues();

}