package org.processmining.plugins.inductiveVisualMiner.cost;

import java.math.BigDecimal;
import java.util.List;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMCanceller;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRow;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DisplayType;

import lpsolve.LpSolve;

public class CostModelComputerImplLP extends CostModelComputerAbstract {

	static {
		System.loadLibrary("lpsolve55");
		System.loadLibrary("lpsolve55j");
	}

	public String getName() {
		return "linear programming";
	}

	public void compute(List<Pair<double[], Double>> data, CostModelAbstract result, IvMCanceller canceller)
			throws Exception {
		/**
		 * Structure of the LP problem: per cost model parameter: 1 variable;
		 * per trace: 1 variable denoting the error & 1 variable denoting the
		 * cumulative sum of absolute values of errors.
		 * 
		 * Objective function is the cumulative sum of absolute values of the
		 * first trace.
		 * 
		 * Notice that the columns are 1-based.
		 */

		int numberOfParameters = result.getNumberOfParameters();
		int numberOfTraces = data.size();
		int numberOfColumns = numberOfParameters + numberOfTraces + numberOfTraces;
		int startColumn_parameters = 1;
		int startColumn_errors = startColumn_parameters + numberOfParameters;
		int startColumn_cumulative = startColumn_errors + numberOfTraces;

		LpSolve solver = LpSolve.makeLp(0, numberOfColumns);

		solver.setDebug(false);
		solver.setVerbose(0);

		solver.setAddRowmode(true);

		//rows: for each trace, the sum of parameters and the errors is the given value
		{
			int traceNr = 0;
			for (Pair<double[], Double> datum : data) {
				double[] constraint = new double[numberOfColumns + 1];

				//parameters
				for (int p = 0; p < numberOfParameters; p++) {
					constraint[startColumn_parameters + p] = datum.getA()[p];
				}

				//error variable
				constraint[startColumn_errors + traceNr] = 1;

				solver.addConstraint(constraint, LpSolve.EQ, datum.getB());

				traceNr++;
			}
		}

		if (canceller.isCancelled()) {
			return;
		}

		//rows: errors can be negative
		for (int traceNumber = 0; traceNumber < data.size(); traceNumber++) {
			solver.setUnbounded(startColumn_errors + traceNumber);
		}

		//objective function: objective function variable (OFV)
		{
			double[] objectiveFunction = new double[numberOfColumns + 1];
			objectiveFunction[startColumn_cumulative] = 1;
			solver.setObjFn(objectiveFunction);
		}

		//row: for each trace, add two cumulative constraints
		{
			for (int traceNr = 0; traceNr < numberOfTraces; traceNr++) {
				//positive sign absolute
				{
					double[] constraint = new double[numberOfColumns + 1];

					constraint[startColumn_errors + traceNr] = 1;
					constraint[startColumn_cumulative + traceNr] = -1;
					if (traceNr < numberOfTraces - 1) {
						constraint[startColumn_cumulative + traceNr + 1] = 1;
					}

					solver.addConstraint(constraint, LpSolve.LE, 0);
				}

				//negative sign absolute
				{
					double[] constraint = new double[numberOfColumns + 1];

					constraint[startColumn_errors + traceNr] = -1;
					constraint[startColumn_cumulative + traceNr] = -1;
					if (traceNr < numberOfTraces - 1) {
						constraint[startColumn_cumulative + traceNr + 1] = 1;
					}

					solver.addConstraint(constraint, LpSolve.LE, 0);
				}

			}
		}

		solver.setAddRowmode(false);

		if (canceller.isCancelled()) {
			return;
		}

		solver.solve();

		//copy parameters
		double[] errors = new double[numberOfTraces];
		{
			double[] var = solver.getPtrVariables();
			double[] parameterValues = new double[numberOfParameters];
			System.arraycopy(var, 0, parameterValues, 0, numberOfParameters);
			result.setParameters(parameterValues);

			System.arraycopy(var, numberOfParameters, errors, 0, numberOfTraces);
		}

		//compute average absolute error
		{
			BigDecimal sum = BigDecimal.ZERO;
			for (double error : errors) {
				sum = sum.add(BigDecimal.valueOf(Math.abs(error)));
			}
			BigDecimal averageAbsoluteError = sum.divide(BigDecimal.valueOf(numberOfTraces), 10,
					BigDecimal.ROUND_HALF_UP);

			result.getModelProperties().add(
					new DataRow<Object>("cost model", "total absolute error", DisplayType.numeric(sum.doubleValue())));
			result.getModelProperties().add(new DataRow<Object>("cost model", "average absolute error",
					DisplayType.numeric(averageAbsoluteError.doubleValue())));
		}

		/**
		 * This call should probably be enabled to clean up, but the Java VM
		 * crashes if we enable it...
		 */
		//solver.deleteAndRemoveLp();

		//set assumptions
		{
			//set information about the cost model
			result.getModelProperties()
					.add(new DataRow<Object>(DisplayType.literal("all parameters ≥ 0"), "cost model", "assumption"));

		}
	}
}