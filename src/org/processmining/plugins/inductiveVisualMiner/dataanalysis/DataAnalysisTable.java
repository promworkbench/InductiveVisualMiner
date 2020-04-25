package org.processmining.plugins.inductiveVisualMiner.dataanalysis;

import javax.swing.JTable;

import org.processmining.plugins.inductiveminer2.attributes.AttributesInfo;

public abstract class DataAnalysisTable<D> extends JTable {

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

	public abstract void setAttributesInfo(AttributesInfo attributesInfo);

	public abstract void setData(D data);
}