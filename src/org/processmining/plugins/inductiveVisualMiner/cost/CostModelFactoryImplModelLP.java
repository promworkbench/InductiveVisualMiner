package org.processmining.plugins.inductiveVisualMiner.cost;

import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogInfo;

public class CostModelFactoryImplModelLP implements CostModelFactory {

	@Override
	public CostModelComputer createComputer() {
		return new CostModelComputerImplLP();
	}

	@Override
	public CostModelAbstract createCostModel(IvMModel model, IvMLogInfo logInfoFiltered) {
		return new CostModelImplModel(model);
	}

}