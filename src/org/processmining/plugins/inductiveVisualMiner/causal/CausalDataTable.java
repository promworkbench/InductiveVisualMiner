package org.processmining.plugins.inductiveVisualMiner.causal;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.custom_hash.TObjectIntCustomHashMap;
import gnu.trove.strategy.HashingStrategy;

/**
 * In a row, a NO_VALUE means that the choice was not encountered. A negative
 * value means that the ~corresponding choice was encountered, but the node was
 * skipped.
 * 
 * @author sander
 *
 */
public class CausalDataTable {

	private final static HashingStrategy<int[]> hashingStrategy = new HashingStrategy<int[]>() {
		private static final long serialVersionUID = 1L;

		public int computeHashCode(int[] object) {
			return Arrays.hashCode(object);
		}

		public boolean equals(int[] o1, int[] o2) {
			return Arrays.equals(o1, o2);
		}
	};

	//result variables
	private final List<Choice> columns;
	private final TObjectIntMap<int[]> rows = new TObjectIntCustomHashMap<>(hashingStrategy, 10, 0.5f, 0);

	public static final int NO_VALUE = Integer.MIN_VALUE;
	public static final String NO_VALUE_STRING = "x";

	public CausalDataTable(List<Choice> columns) {
		this.columns = columns;
	}

	public static int getSkipNode(int node) {
		return ~node;
	}

	public void addRow(int[] currentRow) {
		addRow(currentRow, 1);
	}

	public void addRow(int[] currentRow, int cardinality) {
		rows.adjustOrPutValue(currentRow, cardinality, cardinality);
	}

	public int getNumberOfUniqueRows() {
		return rows.size();
	}

	public TObjectIntIterator<int[]> iterator() {
		return rows.iterator();
	}

	public Set<int[]> getRows() {
		return rows.keySet();
	}

	public List<Choice> getColumns() {
		return Collections.unmodifiableList(columns);
	}

	public String toString(int limit) {
		StringBuilder result = new StringBuilder();

		//header
		for (Iterator<Choice> it = columns.iterator(); it.hasNext();) {
			result.append(it.next());
			if (it.hasNext()) {
				result.append(",");
			}
		}
		result.append("\n");

		//data
		if (limit < 0) {
			limit = Integer.MAX_VALUE;
		}
		int i = 0;
		TObjectIntIterator<int[]> it = rows.iterator();
		while (i < limit && it.hasNext()) {
			it.advance();
			i++;

			int[] row = it.key();
			for (int j = 0; j < columns.size(); j++) {
				if (row[j] == NO_VALUE) {
					result.append(NO_VALUE_STRING);
				} else {
					result.append(row[j]);
				}
				if (j < columns.size() - 1) {
					result.append(",");
				}
			}
			if (i < limit && it.hasNext()) {
				result.append("\n");
			}
		}
		return result.toString();
	}

	@Override
	public String toString() {
		return toString(10);
	}

	public int getNumberOfColumns() {
		return columns.size();
	}
}