package org.processmining.plugins.inductiveVisualMiner.dataanalysis.logattributes;

import javax.swing.table.AbstractTableModel;

import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysisTable;

public class LogAttributeAnalysisTable extends DataAnalysisTable {

	private static final long serialVersionUID = 192566312464894607L;

	private AbstractTableModel model;
	private LogAttributeAnalysis dataAnalysis;

	public LogAttributeAnalysisTable() {
		//fill the table
		model = new AbstractTableModel() {

			private static final long serialVersionUID = 5457412338454004753L;

			public int getColumnCount() {
				return 2;
			}

			public String getColumnName(int column) {
				switch (column) {
					case 0 :
						return "Attribute";
					default :
						return "value";
				}
			}

			public int getRowCount() {
				if (dataAnalysis == null) {
					return 0;
				}
				return dataAnalysis.size();
			}

			public Object getValueAt(int row, int column) {
				if (dataAnalysis == null) {
					return "";
				}

				switch (column) {
					case 0 :
						//attribute name
						return dataAnalysis.get(row).getA();
					default :
						//attribute value
						return dataAnalysis.get(row).getB();
				}
			}

		};

		setModel(model);
	}

	public boolean setData(InductiveVisualMinerState state) {
		dataAnalysis = state.getLogAttributesAnalysis();
		model.fireTableStructureChanged();
		return dataAnalysis != null;
	}

	public void invalidateData() {
		dataAnalysis = null;
		model.fireTableStructureChanged();
	}

}