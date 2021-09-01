package org.processmining.plugins.inductiveVisualMiner.dataanalysis.cost;

import java.util.ArrayList;
import java.util.List;

import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysesView;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRowBlock;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRowBlockComputer;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataTab;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataTable;

public class CostDataTab<C, P> implements DataTab<Object, C, P> {

	public static final String name = "Cost model";
	public static final String explanation = "The parameters of the discovered cost model.\nIf traces are highlighted, two cost models will be computed: for highlighted and not-highlighted traces.";

	@Override
	public DataTable<Object, C, P> createTable(DataAnalysesView<C, P> dataAnalysesView) {
		DataTable<Object, C, P> table = new DataTable<>(name, dataAnalysesView);
		table.getModel().setColumnNames(new String[][] { {}, { "" }, { "", "" }, { "Move type", "activity", "cost" },
				{ "Move type", "activity", "cost highlighted traces", "cost not-highlighted traces" } });
		return table;
	}

	@Override
	public List<DataRowBlock<Object, C, P>> createRowBlocks(DataTable<Object, C, P> table) {
		List<DataRowBlock<Object, C, P>> result = new ArrayList<>();
		result.add(new CostRowBlock<C, P>(table));
		return result;
	}

	@Override
	public List<DataRowBlockComputer<Object, C, P>> createRowBlockComputers() {
		List<DataRowBlockComputer<Object, C, P>> result = new ArrayList<>();
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
		return false;
	}
}
