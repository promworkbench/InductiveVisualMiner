package org.processmining.plugins.inductiveVisualMiner.cost;

import java.util.List;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMCanceller;

public class CostModelComputerImplOLS extends CostModelComputerAbstract {

	public String getName() {
		return "ordinary least squares linear regression";
	}

	public void compute(List<Pair<double[], Double>> data, CostModelAbstract result, IvMCanceller canceller) {

		double[][] inputs = new double[data.size()][];
		{
			int i = 0;
			for (Pair<double[], Double> list : data) {
				inputs[i] = list.getA();
				i++;
			}
		}

		double[] cost = new double[data.size()];
		{
			int i = 0;
			for (Pair<double[], Double> p : data) {
				cost[i] = p.getB();
				i++;
			}
		}

		Regression regression = new Regression();
		double[] coe = regression.regress(inputs, cost);

		if (canceller.isCancelled()) {
			return;
		}

		if (regression.getMessage() != null) {
			//something went wrong; get the error message
			message = regression.getMessage();
			return;
		}

		result.setParameters(coe);
		return;
	}
}