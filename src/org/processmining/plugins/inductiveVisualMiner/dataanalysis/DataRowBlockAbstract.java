package org.processmining.plugins.inductiveVisualMiner.dataanalysis;

import java.util.ArrayList;
import java.util.List;

import org.processmining.plugins.inductiveVisualMiner.chain.DataChainLinkGuiAbstract;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObjectValues;

public abstract class DataRowBlockAbstract<C, P> extends DataChainLinkGuiAbstract<C, P> implements DataRowBlock<C, P> {

	private static final DisplayType computing = DisplayType.literal("[computing..]");

	private final DataTable<C, P> table;
	private List<DataRow> rows;

	public DataRowBlockAbstract(DataTable<C, P> table) {
		this.table = table;
		rows = new ArrayList<>();
	}

	/**
	 * This is performed on the gui thread, so large computations are forbidden:
	 * the user is waiting!
	 * 
	 * @param inputs
	 * @return
	 */
	public abstract List<DataRow> gather(IvMObjectValues inputs);

	@Override
	public DataRow getRow(int row) {
		return rows.get(row);
	}

	@Override
	public int getNumberOfRows() {
		return rows.size();
	}

	@Override
	public void invalidate(P panel) {
		for (DataRow row : rows) {
			row.setAllValues(computing);
		}
		table.rowsChanged();
	}

	@Override
	public void updateGui(P panel, IvMObjectValues inputs) throws Exception {
		int namesOld = getMaxNumberOfNames();
		int valuesOld = getMaxNumberOfValues();
		
		//update
		rows = gather(inputs);
		
		if (namesOld == getMaxNumberOfNames() && valuesOld == getMaxNumberOfValues()) {
			table.rowsChanged();
		} else {
			table.columnsChanged();
		}
	}

	public int getMaxNumberOfNames() {
		if (rows == null) {
			return 0;
		}
		int result = 0;
		for (DataRow row : rows) {
			result = Math.max(result, row.getNumberOfNames());
		}
		return result;
	}

	public int getMaxNumberOfValues() {
		if (rows == null) {
			return 0;
		}
		int result = 0;
		for (DataRow row : rows) {
			result = Math.max(result, row.getNumberOfValues());
		}
		return result;
	}

	public boolean showsSomething() {
		for (DataRow row : rows) {
			for (DisplayType value : row.getValues()) {
				if (!value.equals(computing)) {
					return true;
				}
			}
		}
		return false;
	}
}