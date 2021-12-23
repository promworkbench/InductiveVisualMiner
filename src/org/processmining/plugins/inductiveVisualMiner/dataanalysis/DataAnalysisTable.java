package org.processmining.plugins.inductiveVisualMiner.dataanalysis;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.border.LineBorder;
import javax.swing.table.TableRowSorter;

import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DisplayType.Type;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.traceattributes.CorrelationDensityPlot;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.MultiComboBox;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;

import gnu.trove.map.hash.THashMap;

public class DataAnalysisTable<O, C, P> extends JTable {

	private static final long serialVersionUID = -237386992048858811L;
	public static final int rowHeight = 22;
	private static final int rowHeightImage = CorrelationDensityPlot.getHeight();
	public static final int rowMargin = 3;
	public static final int columnMargin = 5;

	private DataAnalysisView dataAnalysisView;
	private final IvMDecoratorI decorator;
	private final Map<String, DataAnalysisTableRowFilterItem[]> selections;

	public DataAnalysisTable(String tabName, DataAnalysesView<C, P> dataAnalysesView, IvMDecoratorI decorator) {
		super(new DataAnalysisTableModel<O, C, P>(tabName, dataAnalysesView));
		this.decorator = decorator;
		this.selections = new THashMap<>();

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
		setFiltering();
		setRowHeights();
	}

	protected void setSorting() {
		DataAnalysisTableRowSorter<O, C, P> sorter = new DataAnalysisTableRowSorter<>(getModel());
		List<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();

		//set how name columns should be sorted
		for (int column = 0; column < getModel().getNumberOfNameColumns(); column++) {
			sorter.setComparator(column, new Comparator<String>() {
				@Override
				public int compare(String o1, String o2) {
					return o1.toLowerCase().compareTo(o2.toLowerCase());
				}
			});
		}
		//set how value columns should be sorted
		for (int column = getModel().getNumberOfNameColumns(); column < getModel().getColumnCount(); column++) {
			sorter.setComparator(column, new Comparator<DisplayType>() {

				public int compare(DisplayType o1, DisplayType o2) {
					if (o1.getType() != o2.getType()) {
						return o1.getType().compareTo(o2.getType());
					}

					if (o1.getType() == Type.duration) {
						return Long.compare(((DisplayType.Duration) o1).getValueLong(),
								((DisplayType.Duration) o2).getValueLong());
					}

					if (o1.getType() == Type.time) {
						return Long.compare(((DisplayType.Time) o1).getValueLong(), ((DisplayType.Time) o2).getValueLong());
					}

					if (o1.getType() == Type.numeric) {
						return Double.compare(o1.getValue(), o2.getValue());
					}

					return o1.toString().toLowerCase().compareTo(o2.toString().toLowerCase());
				}

			});
		}

		//set default sorting
		setDefaultSorting(sorter, sortKeys);

		sorter.setSortsOnUpdates(true);
		sorter.setSortKeys(sortKeys);
		setRowSorter(sorter);
	}

	protected void setDefaultSorting(TableRowSorter<DataAnalysisTableModel<O, C, P>> sorter,
			List<RowSorter.SortKey> sortKeys) {
		for (int column = 0; column < getModel().getNumberOfNameColumns(); column++) {
			sortKeys.add(new RowSorter.SortKey(column, SortOrder.ASCENDING));
		}
	}

	private void setRowHeights() {
		for (int modelRow = 0; modelRow < getModel().getRowCount(); modelRow++) {
			int viewRow = convertRowIndexToView(modelRow);
			if (viewRow >= 0) {
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
	}

	private void setFiltering() {
		if (dataAnalysisView != null) {
			//create the comboboxes
			final List<MultiComboBox<DataAnalysisTableRowFilterItem>> comboBoxes = new ArrayList<>();
			{
				JPanel panel = dataAnalysisView.getFiltersPanel();
				panel.removeAll();
				{
					JLabel label = new JLabel("Show: ");
					decorator.decorate(label);
					panel.add(label);
				}

				for (int column = 0; column < getModel().getNumberOfNameColumns(); column++) {
					final String columnName = getModel().getColumnName(column);

					JLabel label = new JLabel(columnName);
					decorator.decorate(label);
					panel.add(label);

					panel.add(Box.createRigidArea(new Dimension(3, 0)));

					Set<DataAnalysisTableRowFilterItem> values = new TreeSet<>();
					values.add(DataAnalysisTableRowFilterItem.all());
					for (int row = 0; row < getModel().getRowCount(); row++) {
						values.add(new DataAnalysisTableRowFilterItem(getModel().getRow(row).getName(column)));
					}
					DataAnalysisTableRowFilterItem[] items = values.toArray(new DataAnalysisTableRowFilterItem[values.size()]);
					final MultiComboBox<DataAnalysisTableRowFilterItem> combobox = new MultiComboBox<DataAnalysisTableRowFilterItem>(DataAnalysisTableRowFilterItem.class, items);

					if (selections.containsKey(columnName)) {
						//restore cashed selected items
						combobox.setSelectedItems(selections.get(columnName));
					} else {
						//select all
						combobox.setSelectedItem(DataAnalysisTableRowFilterItem.all());
					}

					decorator.decorate(combobox);
					combobox.setBorder(new LineBorder(decorator.backGroundColour2(), 5));
					combobox.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							//cache the selected values
							selections.put(columnName, combobox.getSelectedObjects());

							//refresh the filter
							getRowSorter().setRowFilter(new DataAnalysisTableRowFilter<O, C, P>(selections));
							setRowHeights();
						}
					});
					panel.add(combobox);

					comboBoxes.add(combobox);

					if (column < getModel().getNumberOfNameColumns() - 1) {
						panel.add(Box.createRigidArea(new Dimension(5, 0)));
					}
				}
			}
			
			//set the filter
			getRowSorter().setRowFilter(new DataAnalysisTableRowFilter<O, C, P>(selections));
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public DataAnalysisTableModel<O, C, P> getModel() {
		return (DataAnalysisTableModel<O, C, P>) super.getModel();
	}

	@SuppressWarnings("unchecked")
	@Override
	public DataAnalysisTableRowSorter<O, C, P> getRowSorter() {
		return (DataAnalysisTableRowSorter<O, C, P>) super.getRowSorter();
	}

	/**
	 * 
	 * @return an object that will be set to true when the tab is switched on.
	 *         If null is returned, the tab will not be switchable.
	 */
	public IvMObject<Boolean> isSwitchable() {
		return null;
	}

	public void setDataAnalysisView(DataAnalysisView dataAnalysisView) {
		this.dataAnalysisView = dataAnalysisView;
	}
}