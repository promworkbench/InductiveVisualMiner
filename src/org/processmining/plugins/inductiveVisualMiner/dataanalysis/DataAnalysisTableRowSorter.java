package org.processmining.plugins.inductiveVisualMiner.dataanalysis;

import javax.swing.table.TableRowSorter;

public class DataAnalysisTableRowSorter<O, C, P> extends TableRowSorter<DataAnalysisTableModel<O, C, P>> {
	public DataAnalysisTableRowSorter(DataAnalysisTableModel<O, C, P> model) {
		super(model);
	}
}