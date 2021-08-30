package org.processmining.plugins.inductiveVisualMiner.cost;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMCanceller;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IteratorWithPosition;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;

import gnu.trove.list.TDoubleList;
import gnu.trove.list.array.TDoubleArrayList;

public class ComputeCostModel {
	public static CostModel compute(IvMModel model, IvMLogFiltered log, IvMCanceller canceller) {
		CostModelAbstract result = new CostModelBasic(model);

		int numberOfTraces = getNumberOfTraces(log);

		OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();

		List<double[]> x = new ArrayList<>(numberOfTraces); //input
		TDoubleList y = new TDoubleArrayList(numberOfTraces); //outcome

		for (IteratorWithPosition<IvMTrace> it = log.iterator(); it.hasNext();) {
			IvMTrace trace = it.next();

			if (canceller.isCancelled()) {
				return null;
			}

			Pair<double[], Double> p = result.getInputsAndCost(trace);

			if (p != null) {
				x.add(p.getA());
				y.add(p.getB());
			}
		}
		
		if (canceller.isCancelled()) {
			return null;
		}

		regression.newSampleData(y.toArray(), (double[][]) x.toArray());
		
		if (canceller.isCancelled()) {
			return null;
		}

		double[] coe = regression.estimateRegressionParameters();
		for (double p : coe) {
			System.out.println(p);
		}

		result.setParameters(coe);
		return result;
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