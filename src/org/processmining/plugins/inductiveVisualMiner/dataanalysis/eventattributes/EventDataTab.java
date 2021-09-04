package org.processmining.plugins.inductiveVisualMiner.dataanalysis.eventattributes;

import java.util.ArrayList;
import java.util.List;

import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysesView;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysisTable;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRowBlock;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRowBlockComputer;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataTab;

public class EventDataTab<C, P> implements DataTab<Object, C, P> {

	public static final String name = "Event attributes";
	public static final String explanation = "Attributes at the event level.\nIf traces are highlighted, attributes will be shown for highlighted and non-highlighted traces.";

	@Override
	public DataAnalysisTable<Object, C, P> createTable(DataAnalysesView<C, P> dataAnalysesView) {
		DataAnalysisTable<Object, C, P> table = new DataAnalysisTable<>(name, dataAnalysesView);
		table.getModel().setColumnNames(new String[][] { {}, { "" }, { "", "" }, { "Attribute", "property", "value" },
				{ "Attribute", "property", "highlighted traces", "not-highlighted traces" } });
		return table;
	}

	@Override
	public List<DataRowBlock<Object, C, P>> createRowBlocks(DataAnalysisTable<Object, C, P> table) {
		List<DataRowBlock<Object, C, P>> result = new ArrayList<>();
		return result;
	}

	@Override
	public List<DataRowBlockComputer<Object, C, P>> createRowBlockComputers() {
		List<DataRowBlockComputer<Object, C, P>> result = new ArrayList<>();
		result.add(new EventDataRowBlock<C, P>());
		result.add(new EventDataRowBlockVirtual<C, P>());
		result.add(new EventDataRowBlockType<C, P>());
		result.add(new EventDataRowBlockTypeVirtual<C, P>());
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
