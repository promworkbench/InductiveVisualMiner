package org.processmining.plugins.inductiveVisualMiner.dataanalysis.modeltime;

import java.util.List;
import java.util.concurrent.Callable;

import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysesView;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysisTabAbstract;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysisTable;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRowBlock;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRowBlockComputer;

public class DataAnalysisTabModelTime<C, P> extends DataAnalysisTabAbstract<Object, C, P> {

	public static final String name = "Model time";
	public static final String explanation = "Analyse the time spent in model activities.";

	public DataAnalysisTabModelTime(Callable<List<DataRowBlock<Object, C, P>>> rowBlocksCreator,
			Callable<List<DataRowBlockComputer<Object, C, P>>> rowBlockComputersCreator) {
		super(rowBlocksCreator, rowBlockComputersCreator);
	}

	@Override
	public DataAnalysisTable<Object, C, P> createTable(DataAnalysesView<C, P> dataAnalysesView) {
		DataAnalysisTable<Object, C, P> table = new DataAnalysisTable<Object, C, P>(name, dataAnalysesView);
		table.getModel()
				.setColumnNames(new String[][] { {}, { "" }, { "", "" }, { "", "", "" },
						{ "Model part", "time type", "", "all traces" },
						{ "Model part", "duration type", "", "highlighted traces", "not-highlighted traces" } });
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