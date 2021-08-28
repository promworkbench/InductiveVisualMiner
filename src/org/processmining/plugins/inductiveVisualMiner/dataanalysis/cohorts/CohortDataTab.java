package org.processmining.plugins.inductiveVisualMiner.dataanalysis.cohorts;

import java.util.ArrayList;
import java.util.List;

import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.table.TableRowSorter;

import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysesView;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRowBlock;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRowBlockComputer;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataTab;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataTable;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataTableModel;

public class CohortDataTab<C, P> implements DataTab<C, P> {
	public static final String name = "Cohort analysis";
	public static final String explanation = "Study influence of trace attributes on process behaviour.\n"
			+ "Click: highlight cohort;\t" + "shift+click: highlight other traces;\t"
			+ "ctrl+click: disable cohort highlighting.";

	@Override
	public DataTable<C, P> createTable(DataAnalysesView<C, P> dataAnalysesView) {
		DataTable<C, P> table = new DataTable<C, P>(name, dataAnalysesView) {
			private static final long serialVersionUID = -6838046929095568195L;

			protected void setDefaultSorting(TableRowSorter<DataTableModel<C, P>> sorter, List<SortKey> sortKeys) {
				if (getColumnCount() >= 4) {
					sortKeys.add(new RowSorter.SortKey(3, SortOrder.DESCENDING));
					sorter.setSortable(1, false);
				}
			}
		};
		table.getModel().setColumnNames(new String[][] { {}, { "" }, { "", "" }, { "", "", "" },
				{ "Cohort attribute", "value range", "number of traces", "distance with rest of log" } });
		return table;
	}

	@Override
	public List<DataRowBlock<C, P>> createRowBlocks(DataTable<C, P> table) {
		List<DataRowBlock<C, P>> result = new ArrayList<>();
		result.add(new CohortRowBlock<C, P>(table));
		return result;
	}

	@Override
	public List<DataRowBlockComputer<C, P>> createRowBlockComputers() {
		List<DataRowBlockComputer<C, P>> result = new ArrayList<>();
		return result;
	}

	@Override
	public String getAnalysisName() {
		return name;
	}

	@Override
	public String getExplanation() {
		return explanation;
	}

	@Override
	public boolean isSwitchable() {
		return true;
	}
}
