package org.processmining.plugins.inductiveVisualMiner.dataanalysis.eventattributes;

import java.util.List;
import java.util.concurrent.Callable;

import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysesView;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysisTabAbstract;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysisTable;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRowBlock;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRowBlockComputer;

public class EventDataTab<C, P> extends DataAnalysisTabAbstract<Object, C, P> {

	public static final String name = "Event attributes";
	public static final String explanation = "Attributes at the event level.\nIf traces are highlighted, attributes will be shown for highlighted and non-highlighted traces.";

	public EventDataTab(Callable<List<DataRowBlock<Object, C, P>>> rowBlocksCreator,
			Callable<List<DataRowBlockComputer<Object, C, P>>> rowBlockComputersCreator) {
		super(rowBlocksCreator, rowBlockComputersCreator);
	}

	@Override
	public DataAnalysisTable<Object, C, P> createTable(DataAnalysesView<C, P> dataAnalysesView) {
		DataAnalysisTable<Object, C, P> table = new DataAnalysisTable<>(name, dataAnalysesView);
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

	@Override
	public boolean isSwitchable() {
		return false;
	}
}
