package org.processmining.plugins.inductiveVisualMiner.dataanalysis.eventattributes;

import java.util.List;
import java.util.concurrent.Callable;

import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysesView;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysisTabAbstract;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysisTable;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRowBlock;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRowBlockComputer;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;

public class DataAnalysisTabEventData<C, P> extends DataAnalysisTabAbstract<Object, C, P> {

	public static final String name = "Event attributes";
	public static final String explanation = "Attributes at the event level.\nIf traces are highlighted, attributes will be shown for highlighted and non-highlighted traces.";

	public DataAnalysisTabEventData(Callable<List<DataRowBlock<Object, C, P>>> rowBlocksCreator,
			Callable<List<DataRowBlockComputer<Object, C, P>>> rowBlockComputersCreator) {
		super(rowBlocksCreator, rowBlockComputersCreator);
	}

	@Override
	public DataAnalysisTable<Object, C, P> createTable(DataAnalysesView<C, P> dataAnalysesView,
			IvMDecoratorI decorator) {
		DataAnalysisTable<Object, C, P> table = new DataAnalysisTable<>(name, dataAnalysesView, decorator);
		table.getModel().setColumnNames(new String[][] { {}, { "" }, { "", "" }, { "Attribute", "property", "value" },
				{ "Attribute", "property", "highlighted traces", "not-highlighted traces" } });
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
