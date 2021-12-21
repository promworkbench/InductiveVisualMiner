package org.processmining.plugins.inductiveVisualMiner.dataanalysis;

public class DataAnalysisTableRowFilterItem implements Comparable<DataAnalysisTableRowFilterItem> {
	final String content;

	public DataAnalysisTableRowFilterItem(String value) {
		content = value;
	}

	public static DataAnalysisTableRowFilterItem all() {
		return new DataAnalysisTableRowFilterItem(null);
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

	public int compareTo(DataAnalysisTableRowFilterItem o) {
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
		DataAnalysisTableRowFilterItem other = (DataAnalysisTableRowFilterItem) obj;
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