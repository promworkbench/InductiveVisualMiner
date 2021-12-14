package org.processmining.plugins.inductiveVisualMiner.dataanalysis;

import java.util.List;

import javax.swing.RowFilter;

import org.processmining.plugins.inductiveVisualMiner.helperClasses.MultiComboBox;

public class DataAnalysisTableRowFilter<O, C, P> extends RowFilter<DataAnalysisTableModel<O, C, P>, Integer> {

	private List<MultiComboBox<Item>> comboboxes;

	public static class Item implements Comparable<Item> {
		final String content;

		public Item(String value) {
			content = value;
		}

		public static Item all() {
			return new Item(null);
		}

		public String toString() {
			if (content == null) {
				return "[all]";
			}
			return content.toString();
		}

		public boolean isAll() {
			return content == null;
		}

		public int compareTo(Item o) {
			if (content == null && o.content == null) {
				return 0;
			} else if (content == null) {
				return -1;
			} else if (o.content == null) {
				return 1;
			}
			return content.compareTo(o.content);
		}

		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((content == null) ? 0 : content.hashCode());
			return result;
		}

		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			Item other = (Item) obj;
			if (content == null) {
				if (other.content != null) {
					return false;
				}
			} else if (!content.equals(other.content)) {
				return false;
			}
			return true;
		}

	}

	public DataAnalysisTableRowFilter(List<MultiComboBox<Item>> comboboxes) {
		this.comboboxes = comboboxes;
	}

	public boolean include(Entry<? extends DataAnalysisTableModel<O, C, P>, ? extends Integer> entry) {
		DataAnalysisTableModel<O, C, P> model = entry.getModel();
		int row = entry.getIdentifier();
		for (int column = 0; column < model.getNumberOfNameColumns(); column++) {
			String value = model.getValueAt(row, column).toString();
			if (!isSelected(comboboxes.get(column).getSelectedObjects(), value)) {
				return false;
			}
		}
		return true;
	}

	public static boolean isSelected(Item[] items, String value) {
		for (Item item : items) {
			if (item.isAll() || item.toString().equals(value)) {
				return true;
			}
		}
		return false;
	}
}