package org.processmining.plugins.inductiveVisualMiner.cost;

import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;

public class ComputeCostModel {
	public static CostModelBasic compute(IvMModel model, IvMLogFiltered log) {
		return new CostModelBasic(model, 1, 0, 1);
	}

}