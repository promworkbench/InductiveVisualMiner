package org.processmining.plugins.inductiveVisualMiner.dataanalysis.logattributes;

import javax.swing.table.AbstractTableModel;

import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObjectValues;
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

	@SuppressWarnings("unchecked")
	public boolean setData(IvMObjectValues inputs) {
		dataAnalysis = inputs.get(IvMObject.data_analysis_log);

		if (inputs.has(IvMObject.data_analysis_log_virtual_attributes)) {
			dataAnalysis.addVirtualAttributes(inputs.get(IvMObject.data_analysis_log_virtual_attributes));
		} else {
			dataAnalysis.setVirtualAttributesToPlaceholders();
		}

		model.fireTableStructureChanged();
		return dataAnalysis != null;
	}

	public void invalidateData() {
		dataAnalysis = null;
		model.fireTableStructureChanged();
	}

}