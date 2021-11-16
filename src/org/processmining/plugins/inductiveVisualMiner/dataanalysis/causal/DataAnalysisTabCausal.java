package org.processmining.plugins.inductiveVisualMiner.dataanalysis.causal;

import java.util.List;
import java.util.concurrent.Callable;

import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysesView;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysisTabAbstract;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysisTable;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRowBlock;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRowBlockComputer;

public class DataAnalysisTabCausal<C, P> extends DataAnalysisTabAbstract<Object, C, P> {

	public static final String name = "Causal";
	public static final String explanation = "Study influence of decisions in the model on one another.";

	public DataAnalysisTabCausal(Callable<List<DataRowBlock<Object, C, P>>> rowBlocksCreator,
			Callable<List<DataRowBlockComputer<Object, C, P>>> rowBlockComputersCreator) {
		super(rowBlocksCreator, rowBlockComputersCreator);
	}

	@Override
	public DataAnalysisTable<Object, C, P> createTable(DataAnalysesView<C, P> dataAnalysesView) {
		DataAnalysisTable<Object, C, P> table = new DataAnalysisTable<Object, C, P>(name, dataAnalysesView);
		table.getModel().setColumnNames(new String[][] { {}, { "" }, { "Unfolding", "choice between" }, { "", "", "" },
				{ "Unfolding", "choice between", ".", "." } });
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
