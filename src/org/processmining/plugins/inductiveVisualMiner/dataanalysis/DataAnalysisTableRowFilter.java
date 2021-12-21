package org.processmining.plugins.inductiveVisualMiner.dataanalysis;

import java.util.Map;

import javax.swing.RowFilter;

public class DataAnalysisTableRowFilter<O, C, P> extends RowFilter<DataAnalysisTableModel<O, C, P>, Integer> {

	private final Map<String, DataAnalysisTableRowFilterItem[]> selectedItems;

	public DataAnalysisTableRowFilter(Map<String, DataAnalysisTableRowFilterItem[]> selectedItems) {
		this.selectedItems = selectedItems;
	}

	public boolean include(Entry<? extends DataAnalysisTableModel<O, C, P>, ? extends Integer> entry) {
		DataAnalysisTableModel<O, C, P> model = entry.getModel();
		int row = entry.getIdentifier();
		for (int column = 0; column < model.getNumberOfNameColumns(); column++) {
			String value = model.getValueAt(row, column).toString();
			String columnName = model.getColumnName(column);
			if (selectedItems.containsKey(columnName) && !isSelected(selectedItems.get(columnName), value)) {
				return false;
			}
		}
		return true;
	}

	public static boolean isSelected(DataAnalysisTableRowFilterItem[] items, String value) {
		for (DataAnalysisTableRowFilterItem item : items) {
			if (item.isAll() || item.toString().equals(value)) {
				return true;
			}
		}
		return false;
	}
}