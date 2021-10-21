package org.processmining.plugins.inductiveVisualMiner.cost;

import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogInfo;

public class CostModelFactoryImplModelDeviationsServiceLP implements CostModelFactory {

	public CostModelComputer createComputer() {
		return new CostModelComputerImplLP();
	}

	public CostModelAbstract createCostModel(IvMModel model, IvMLogInfo logInfoFiltered) {
		return new CostModelImplModelDeviationsService(model, logInfoFiltered);
	}

}