package org.processmining.plugins.inductiveVisualMiner.causal;

import java.util.Set;

import org.apache.commons.math3.linear.SingularMatrixException;
import org.apache.commons.math3.stat.regression.GLSMultipleLinearRegression;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMCanceller;

import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.set.TDoubleSet;
import gnu.trove.set.hash.TDoubleHashSet;

public class CausalRegression {

	static {
		System.loadLibrary("lpsolve55");
		System.loadLibrary("lpsolve55j");
	}

	public static CausalAnalysisResult compute(CausalGraph binaryCausalGraph, CausalDataTable binaryChoiceData,
			IvMCanceller canceller) {

		CausalAnalysisResult result = new CausalAnalysisResult();

		for (Pair<Choice, Choice> edge : binaryCausalGraph.getEdges()) {
			Choice source = edge.getA();
			Choice target = edge.getB();

			double causalStrength = computeCausalStrength(binaryCausalGraph, binaryChoiceData, source, target,
					canceller);

			if (!Double.isNaN(causalStrength)) {
				result.addEdgeCausal(Pair.of(source, target), causalStrength);
			}
		}

		return result;
	}

	private static double computeCausalStrength(CausalGraph binaryCausalGraph, CausalDataTable binaryChoiceData,
			Choice source, Choice target, IvMCanceller canceller) {
		Set<Choice> adjustmentSet = CausalBackdoorCriterion.compute(binaryCausalGraph, binaryChoiceData, source,
				target);
		int sourceColumn = binaryChoiceData.getColumns().indexOf(source);
		int targetColumn = binaryChoiceData.getColumns().indexOf(target);

		int targetNode = target.nodes.iterator().next();

		if (adjustmentSet == null) {
			return Double.NaN;
		}

		//create a map variable -> column
		int[] variable2column = new int[adjustmentSet.size() + 1];
		int variables;
		{
			variable2column[0] = sourceColumn;
			int variable = 1;
			for (int column = 0; column < binaryChoiceData.getNumberOfColumns(); column++) {
				if (adjustmentSet.contains(binaryChoiceData.getColumns().get(column))) {
					variable2column[variable] = column;
					variable++;
				}
			}
			variables = variable;
		}

		if (canceller.isCancelled()) {
			return Double.NaN;
		}

		//find out how many rows encountered the target choice
		int numberOfRows = 0;
		{
			for (int[] row : binaryChoiceData.getRows()) {
				if (row[targetColumn] != CausalDataTable.NO_VALUE) {
					numberOfRows++;
				}
			}
		}

		//create targets
		double[] regressands = new double[numberOfRows];
		{
			int rowIndex = 0;
			for (int[] row : binaryChoiceData.getRows()) {
				if (row[targetColumn] != CausalDataTable.NO_VALUE) {
					regressands[rowIndex] = row[targetColumn] == targetNode ? 1 : 0;
					rowIndex++;
				}
			}
		}

		if (canceller.isCancelled()) {
			return Double.NaN;
		}

		//create influencers
		double[][] regressors = new double[numberOfRows][variables];
		{
			int rowIndex = 0;
			for (int[] row : binaryChoiceData.getRows()) {
				if (row[targetColumn] != CausalDataTable.NO_VALUE) {
					for (int variable = 0; variable < variables; variable++) {
						int column = variable2column[variable];
						Choice choice = binaryChoiceData.getColumns().get(column);

						regressors[rowIndex][variable] = row[column] == choice.nodes.iterator().next() ? 1 : 0;
					}

					rowIndex++;
				}
			}
		}

		//check that the variables have variance; otherwise there's no point in continuing
		{
			TDoubleSet[] seenValues = new TDoubleSet[variables];
			for (int variable = 0; variable < variables; variable++) {
				seenValues[variable] = new TDoubleHashSet();
			}
			for (double[] regrow : regressors) {
				for (int variable = 0; variable < variables; variable++) {
					seenValues[variable].add(regrow[variable]);
				}
			}

			for (int variable = 0; variable < variables; variable++) {
				if (seenValues[variable].size() < 2) {
					return Double.NaN;
				}
			}
		}

		//create weights
		double[][] covariance = new double[numberOfRows][numberOfRows];
		{
			TObjectIntIterator<int[]> it = binaryChoiceData.iterator();
			int rowIndex = 0;
			while (it.hasNext()) {
				it.advance();
				int[] row = it.key();
				if (row[targetColumn] != CausalDataTable.NO_VALUE) {
					covariance[rowIndex][rowIndex] = it.value();
					rowIndex++;
				}
			}
		}

		GLSMultipleLinearRegression regression = new GLSMultipleLinearRegression();
		regression.setNoIntercept(false);
		regression.newSampleData(regressands, regressors, covariance);

		if (canceller.isCancelled()) {
			return Double.NaN;
		}

		try {
			double[] beta = regression.estimateRegressionParameters();
			return beta[1];
		} catch (SingularMatrixException e) {
			return Double.NaN;
		}

	}
}