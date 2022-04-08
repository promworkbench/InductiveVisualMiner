package org.processmining.plugins.inductiveVisualMiner.dataanalysis.causal;

import java.util.ArrayList;
import java.util.List;

import org.processmining.plugins.inductiveVisualMiner.causal.CausalDataTable;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObjectValues;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRow;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRowBlockAbstract;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DisplayType;

public class DataRowBlockCausalDataTable<C, P> extends DataRowBlockAbstract<Object, C, P> {

	@Override
	public String getName() {
		return "causal data table";
	}

	@Override
	public IvMObject<?>[] createInputObjects() {
		return new IvMObject<?>[] { IvMObject.data_analysis_causal_data_table };
	}

	@Override
	public List<DataRow<Object>> gather(IvMObjectValues inputs) {
		CausalDataTable causalTable = inputs.get(IvMObject.data_analysis_causal_data_table);

		List<DataRow<Object>> result = new ArrayList<>();

		//output the graph in Dot format for export
		result.add(new DataRow<>("causal data table", DisplayType.literal(causalTable.toString(-1))));

		return result;
	}

}
