package org.processmining.plugins.inductiveVisualMiner.dataanalysis.cost;

import java.util.ArrayList;
import java.util.List;

import org.processmining.plugins.inductiveVisualMiner.chain.IvMCanceller;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObjectValues;
import org.processmining.plugins.inductiveVisualMiner.cost.CostModels;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRow;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRowBlockAbstract;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataTable;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DisplayType;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.traceattributes.TraceDataRowBlock;

public class CostRowBlock<C, P> extends DataRowBlockAbstract<Object, C, P> {

	public CostRowBlock(DataTable<Object, C, P> table) {
		super(table);
	}

	@Override
	public String getName() {
		return "cost";
	}

	@Override
	public IvMObject<?>[] createInputObjects() {
		return new IvMObject<?>[] { IvMObject.cost_models };
	}

	@Override
	public List<DataRow<Object>> gather(IvMObjectValues inputs) {
		CostModels costModels = inputs.get(IvMObject.cost_models);
		if (!costModels.isBothModelsWereAttempted()) {
			//one model
			if (costModels.getCostModel() != null) {
				return costModels.getCostModel().getModelRepresentation();
			} else {
				List<DataRow<Object>> result = new ArrayList<>();
				result.add(new DataRow<Object>("cost model property", "was not computed because",
						DisplayType.literal(costModels.getCostModelMessage())));
				return result;
			}
		} else {
			//two models
			List<DataRow<Object>> a;
			List<DataRow<Object>> b;
			if (costModels.getCostModel() != null && costModels.getNegativeCostModel() != null) {
				//both available
				a = costModels.getCostModel().getModelRepresentation();
				b = costModels.getNegativeCostModel().getModelRepresentation();
			} else if (costModels.getCostModel() != null) {
				//only highlighted one
				a = costModels.getCostModel().getModelRepresentation();
				b = costModels.getCostModel().getModelRepresentation();

				//erase a
				for (DataRow<Object> row : b) {
					row.setAllValues(DisplayType.NA());
				}
			} else if (costModels.getNegativeCostModel() != null) {
				//only not-highlighted one
				a = costModels.getNegativeCostModel().getModelRepresentation();
				b = costModels.getNegativeCostModel().getModelRepresentation();

				//erase a
				for (DataRow<Object> row : a) {
					row.setAllValues(DisplayType.NA());
				}
			} else {
				//neither: empty list
				a = new ArrayList<>();
				b = new ArrayList<>();
			}

			List<DataRow<Object>> result = TraceDataRowBlock.merge(a, b, new IvMCanceller(null) {
				@Override
				public boolean isCancelled() {
					return false;
				}
			});

			if (costModels.getCostModelMessage() != null || costModels.getNegativeCostModelMessage() != null) {
				//something went wrong somewhere
				result.add(new DataRow<Object>("cost model property", "was not computed because",
						DisplayType.literal(costModels.getCostModelMessage()),
						DisplayType.literal(costModels.getNegativeCostModelMessage())));
			}

			return result;
		}
	}
}
