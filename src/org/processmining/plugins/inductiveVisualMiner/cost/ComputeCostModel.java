package org.processmining.plugins.inductiveVisualMiner.cost;

import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;

public class ComputeCostModel {
	public static CostModelBasic compute(IvMModel model, IvMLogFiltered log) {
		
		LeastSquaresBuilder b = new LeastSquaresBuilder();
		
		
		
		return new CostModelBasic(model, 1, 0, 1);
	}

}