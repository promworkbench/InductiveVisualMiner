package org.processmining.plugins.inductiveVisualMiner.dataanalysis.logattributes;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysesView;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysisTabAbstract;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysisTable;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRowBlock;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRowBlockComputer;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;

public class DataAnalysisTabLog<C, P> extends DataAnalysisTabAbstract<Object, C, P> {

	public static final String name = "Log attributes";
	public static final String explanation = "Attributes at the log level.";

	public DataAnalysisTabLog(Callable<List<DataRowBlock<Object, C, P>>> rowBlocksCreator,
			Callable<List<DataRowBlockComputer<Object, C, P>>> rowBlockComputersCreator) {
		super(rowBlocksCreator, rowBlockComputersCreator);
	}

	@Override
	public DataAnalysisTable<Object, C, P> createTable(DataAnalysesView<C, P> dataAnalysesView,
			IvMDecoratorI decorator) {
		DataAnalysisTable<Object, C, P> table = new DataAnalysisTable<>(name, dataAnalysesView, decorator);
		table.getModel().setColumnNames(new String[][] { {}, { "" }, { "Attribute", "value" } });
		return table;
	}

	@Override
	public List<DataRowBlockComputer<Object, C, P>> createRowBlockComputers() {
		List<DataRowBlockComputer<Object, C, P>> result = new ArrayList<>();
		result.add(new DataRowBlockLogAttributes<C, P>());
		result.add(new DataRowBlockLogAttributesHighlighted<C, P>());
		result.add(new DataRowBlockLogEMSC<C, P>());
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

}