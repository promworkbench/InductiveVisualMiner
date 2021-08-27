package org.processmining.plugins.inductiveVisualMiner.dataanalysis;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.TableRowSorter;

import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DisplayType.Type;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.traceattributes.CorrelationDensityPlot;

public class DataTable<C, P> extends JTable {

	private static final long serialVersionUID = -237386992048858811L;
	public static final int rowHeight = 22;
	private static final int rowHeightImage = CorrelationDensityPlot.getHeight();
	public static final int rowMargin = 3;
	public static final int columnMargin = 5;

	public DataTable(String tabName, DataAnalysesView<C, P> dataAnalysesView) {
		super(new DataTableModel<C, P>(tabName, dataAnalysesView));

		setOpaque(false);
		setShowGrid(false);
		setRowMargin(rowMargin);
		setRowHeight(rowHeight);
		getColumnModel().setColumnMargin(columnMargin);
		setDefaultRenderer(Object.class, new DataAnalysisTableCellRenderer());
		setColumnSelectionAllowed(false);
		setRowSelectionAllowed(false);

		setSorting();
	}

	/**
	 * Notify the table that the rows might have changed.
	 */
	public void rowsChanged() {
		getModel().mightEnable();
		getModel().fireTableDataChanged();
		setRowHeights();
	}

	/**
	 * Notify the table that the columns might have changed.
	 */
	public void columnsChanged() {
		getModel().mightEnable();
		getModel().fireTableStructureChanged();
		setSorting();
		setRowHeights();
	}

	private void setSorting() {
		TableRowSorter<DataTableModel<C, P>> sorter = new TableRowSorter<>(getModel());
		List<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();

		for (int column = 0; column < getModel().getNumberOfNameColumns(); column++) {
			sorter.setComparator(column, new Comparator<String>() {
				@Override
				public int compare(String o1, String o2) {
					return o1.toLowerCase().compareTo(o2.toLowerCase());
				}
			});
			sortKeys.add(new RowSorter.SortKey(column, SortOrder.ASCENDING));
		}

		sorter.setSortsOnUpdates(true);
		sorter.setSortKeys(sortKeys);
		setRowSorter(sorter);
	}

	private void setRowHeights() {
		for (int modelRow = 0; modelRow < getModel().getRowCount(); modelRow++) {
			boolean hasImage = false;
			for (int column = 0; column < getModel().getColumnCount(); column++) {
				Object value = getModel().getValueAt(modelRow, column);
				if (value instanceof DisplayType && ((DisplayType) value).getType() == Type.image
						&& ((DisplayType.Image) value).getImage() != null) {
					hasImage = true;
					break;
				}
			}

			if (hasImage) {
				setRowHeight(convertRowIndexToView(modelRow), rowHeightImage);
			} else {
				setRowHeight(convertRowIndexToView(modelRow), rowHeight);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public DataTableModel<C, P> getModel() {
		return (DataTableModel<C, P>) super.getModel();
	}

}
