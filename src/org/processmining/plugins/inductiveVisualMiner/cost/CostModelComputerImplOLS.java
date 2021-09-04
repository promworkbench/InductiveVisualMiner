package org.processmining.plugins.inductiveVisualMiner.cost;

import java.util.ArrayList;
import java.util.List;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMCanceller;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IteratorWithPosition;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogInfo;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;

import gnu.trove.list.TDoubleList;
import gnu.trove.list.array.TDoubleArrayList;

public class CostModelComputerImplOLS extends CostModelComputerAbstract {

	public String getName() {
		return "ordinary least squares linear regression";
	}

	public void compute(IvMModel model, IvMLogFiltered log, IvMLogInfo logInfoFiltered, CostModelAbstract result,
			IvMCanceller canceller) {

		int numberOfTraces = getNumberOfTraces(log);

		List<double[]> x = new ArrayList<>(numberOfTraces); //input
		TDoubleList y = new TDoubleArrayList(numberOfTraces); //outcome

		for (IteratorWithPosition<IvMTrace> it = log.iterator(); it.hasNext();) {
			IvMTrace trace = it.next();

			if (canceller.isCancelled()) {
				return;
			}

			Pair<double[], Double> p = result.getInputsAndCost(trace, canceller);

			if (p != null) {
				x.add(p.getA());
				y.add(p.getB());
			}
		}

		if (canceller.isCancelled()) {
			return;
		}

		double[][] xx = new double[x.size()][];
		Regression regression = new Regression();
		double[] coe = regression.regress(x.toArray(xx), y.toArray());

		if (canceller.isCancelled()) {
			return;
		}

		if (coe == null) {
			//something went wrong; get the error message
			message = regression.getMessage();
			return;
		}

		result.setParameters(coe);
		result.setQualityMetrics(new ArrayList<>(regression.getQualityMetrics()));
		return;
	}

	public static int getNumberOfTraces(IvMLogFiltered log) {
		int numberOfTraces = 0;
		for (IteratorWithPosition<IvMTrace> it = log.iterator(); it.hasNext();) {
			it.next();
			numberOfTraces++;
		}
		return numberOfTraces;
	}
}