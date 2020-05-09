package org.processmining.plugins.inductiveVisualMiner.dataanalysis;

import javax.swing.JTable;

import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;

public abstract class DataAnalysisTable extends JTable {

	public static final long serialVersionUID = -7487576728854691713L;
	public static final int rowHeight = 22;
	public static final int rowMargin = 3;
	public static final int columnMargin = 5;

	public DataAnalysisTable() {
		setOpaque(false);
		setShowGrid(false);
		setRowMargin(rowMargin);
		setRowHeight(rowHeight);
		getColumnModel().setColumnMargin(columnMargin);
		setDefaultRenderer(Object.class, new DataAnalysisTableCellRenderer());
		setColumnSelectionAllowed(false);
		setRowSelectionAllowed(false);
	}

	/**
	 * Set the data of this data analysis. Returns whether all necessary data
	 * was present in the state (which is used in file-export).
	 * 
	 * @param state
	 * @return
	 */
	public abstract boolean setData(InductiveVisualMinerState state);

	public abstract void invalidateData();
}