package org.processmining.plugins.inductiveVisualMiner.dataanalysis.logattributes;

import java.util.ArrayList;
import java.util.List;

import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysesView;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRowBlock;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRowBlockComputer;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataTab;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataTable;

public class DataTabLog<C, P> implements DataTab<Object, C, P> {

	public static final String name = "Log attributes";
	public static final String explanation = "Attributes at the log level.";

	@Override
	public DataTable<Object, C, P> createTable(DataAnalysesView<C, P> dataAnalysesView) {
		DataTable<Object, C, P> table = new DataTable<>(name, dataAnalysesView);
		table.getModel().setColumnNames(new String[][] { {}, { "" }, { "Attribute", "value" } });
		return table;
	}

	@Override
	public List<DataRowBlock<Object, C, P>> createRowBlocks(DataTable<Object, C, P> table) {
		List<DataRowBlock<Object, C, P>> result = new ArrayList<>();
		return result;
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

	@Override
	public boolean isSwitchable() {
		return false;
	}

}