package org.processmining.plugins.inductiveVisualMiner.cost;

import java.util.ArrayList;
import java.util.List;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMCanceller;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRow;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DisplayType;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IteratorWithPosition;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogInfo;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;

import lpsolve.LpSolve;
import lpsolve.LpSolveException;

public class ComputeCostModelLP implements ComputeCostModel {

	static {
		System.loadLibrary("lpsolve55");
		System.loadLibrary("lpsolve55j");
	}

	public Pair<CostModel, String> compute(IvMModel model, IvMLogFiltered log, IvMLogInfo logInfoFiltered,
			IvMCanceller canceller) throws LpSolveException {
		CostModelAbstract result = new CostModelWithTime(model, logInfoFiltered);

		//set information about the cost model
		result.getModelProperties().add(new DataRow<Object>(DisplayType.literal("linear programming"), "cost model", "type"));
		result.getModelProperties()
				.add(new DataRow<Object>(DisplayType.literal("all parameters ≥ 0"), "cost model", "assumption"));

		//gather trace data
		List<Pair<double[], Double>> data = new ArrayList<>();
		{
			for (IteratorWithPosition<IvMTrace> it = log.iterator(); it.hasNext();) {
				IvMTrace trace = it.next();

				if (canceller.isCancelled()) {
					return null;
				}

				Pair<double[], Double> p = result.getInputsAndCost(trace, canceller);

				if (p != null) {
					data.add(p);
				}
			}
		}

		if (canceller.isCancelled()) {
			return null;
		}

		/**
		 * Structure of the LP problem: columns [1-p] are the parameters, while
		 * the columns [p+1-p+n] are the errors (where n is the number of traces
		 * with data).
		 * 
		 * Objective function is the sum of the errors.
		 * 
		 * Notice that the columns are 1-based.
		 */

		int numberOfParameters = result.getNumberOfParameters();
		int numberOfTraces = data.size();
		int numberOfColumns = numberOfParameters + numberOfTraces;

		LpSolve solver = LpSolve.makeLp(0, numberOfColumns);

		solver.setDebug(false);
		solver.setVerbose(0);

		solver.setAddRowmode(true);

		//objective function: sum of the errors
		{
			double[] objectiveFunction = new double[numberOfColumns + 1];
			for (int i = numberOfParameters + 1; i < objectiveFunction.length; i++) {
				objectiveFunction[i] = 1;
			}
			solver.setObjFn(objectiveFunction);
		}

		if (canceller.isCancelled()) {
			return null;
		}

		//rows: all parameters > 0
		{
			for (int p = 0; p < numberOfParameters; p++) {
				double[] constraint = new double[numberOfColumns + 1];
				constraint[1 + p] = 1;

				solver.addConstraint(constraint, LpSolve.GE, 0);
			}
		}

		if (canceller.isCancelled()) {
			return null;
		}

		//rows: one per trace
		{
			int traceNr = 0;
			for (Pair<double[], Double> datum : data) {
				double[] constraint = new double[numberOfColumns + 1];

				//error variable
				constraint[1 + numberOfParameters + traceNr] = 1;

				//parameters
				for (int p = 0; p < numberOfParameters; p++) {
					constraint[1 + p] = datum.getA()[p];
				}

				solver.addConstraint(constraint, LpSolve.EQ, datum.getB());

				traceNr++;
			}
		}

		if (canceller.isCancelled()) {
			return null;
		}

		solver.setAddRowmode(false);

		if (canceller.isCancelled()) {
			return null;
		}

		solver.solve();

		//copy parameters
		{
			double[] var = solver.getPtrVariables();
			double[] parameterValues = new double[numberOfParameters];
			System.arraycopy(var, 1, parameterValues, 0, numberOfParameters);
			result.setParameters(parameterValues);
		}

		//get quality measures
		{
			List<Pair<String, Double>> qualityMetrics = new ArrayList<>();

			double valueObjectiveFunction = solver.getObjective();
			qualityMetrics.add(Pair.of("total error", valueObjectiveFunction));
			qualityMetrics.add(Pair.of("average error per trace", valueObjectiveFunction / data.size()));

			result.setQualityMetrics(qualityMetrics);
		}

		return Pair.of((CostModel) result, null);
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