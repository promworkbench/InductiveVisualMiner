package org.processmining.plugins.inductiveVisualMiner.dataanalysis;

import java.util.ArrayList;
import java.util.List;

import org.processmining.plugins.inductiveVisualMiner.chain.DataChainLinkGuiAbstract;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObjectValues;

public abstract class DataRowBlockAbstract<O, C, P> extends DataChainLinkGuiAbstract<C, P>
		implements DataRowBlock<O, C, P> {

	private static final DisplayType computing = DisplayType.literal("[computing..]");

	private final DataTable<O, C, P> table;
	private List<DataRow<O>> rows;

	public DataRowBlockAbstract(DataTable<O, C, P> table) {
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
	public abstract List<DataRow<O>> gather(IvMObjectValues inputs);

	@Override
	public DataRow<O> getRow(int row) {
		return rows.get(row);
	}

	@Override
	public int getNumberOfRows() {
		return rows.size();
	}

	@Override
	public void invalidate(P panel) {
		for (DataRow<O> row : rows) {
			row.setAllValues(computing);
			row.setPayload(null);
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
		for (DataRow<O> row : rows) {
			result = Math.max(result, row.getNumberOfNames());
		}
		return result;
	}

	public int getMaxNumberOfValues() {
		if (rows == null) {
			return 0;
		}
		int result = 0;
		for (DataRow<O> row : rows) {
			result = Math.max(result, row.getNumberOfValues());
		}
		return result;
	}

	public boolean showsSomething() {
		for (DataRow<O> row : rows) {
			for (DisplayType value : row.getValues()) {
				if (!value.equals(computing)) {
					return true;
				}
			}
		}
		return false;
	}
}