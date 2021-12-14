package org.processmining.plugins.inductiveVisualMiner.dataanalysis.cost;

import java.util.List;
import java.util.concurrent.Callable;

import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysesView;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysisTabAbstract;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysisTable;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRowBlock;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRowBlockComputer;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;

public class CostDataTab<C, P> extends DataAnalysisTabAbstract<Object, C, P> {

	public static final String name = "Cost model";
	public static final String explanation = "The parameters of the discovered cost model.\nIf traces are highlighted, two cost models will be computed: for highlighted and not-highlighted traces.\nPress ctrl+c to change the cost model (e.g. to include time).";

	public CostDataTab(Callable<List<DataRowBlock<Object, C, P>>> rowBlocksCreator,
			Callable<List<DataRowBlockComputer<Object, C, P>>> rowBlockComputersCreator) {
		super(rowBlocksCreator, rowBlockComputersCreator);
	}

	@Override
	public DataAnalysisTable<Object, C, P> createTable(DataAnalysesView<C, P> dataAnalysesView,
			IvMDecoratorI decorator) {
		DataAnalysisTable<Object, C, P> table = new DataAnalysisTable<>(name, dataAnalysesView, decorator);
		table.getModel().setColumnNames(new String[][] { {}, { "" }, { "", "" }, { "", "", "cost" },
				{ "", "", "highlighted traces", "not-highlighted traces" } });

		return table;
	}

	@Override
	public String getAnalysisName() {
		return name;
	}

	@Override
	public String getExplanation() {
		return explanation;
	}
}
