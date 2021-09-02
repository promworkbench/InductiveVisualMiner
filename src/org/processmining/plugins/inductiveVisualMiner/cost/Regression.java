package org.processmining.plugins.inductiveVisualMiner.cost;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.apache.commons.math3.linear.SingularMatrixException;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import org.processmining.plugins.InductiveMiner.Pair;

public class Regression {

	private String message;
	private final List<Pair<String, Double>> qualityMetrics = new ArrayList<>();

	public double[] regress(double[][] inputs, double[] outputs) {
		message = null;
		getQualityMetrics().clear();

		if (inputs.length == 0) {
			message = "no cost data available";
			return null;
		}

		//filter zero-columns
		BitSet includedColumns = findNonConstantColumns(inputs);

		//filter duplicate columns
		includedColumns.and(findUniqueColumns(inputs));

		if (includedColumns.isEmpty()) {
			message = "model is not variable enough: no varying parameters";
			return null;
		}

		int[] oldColumn2newColumn = getOldColumn2newColumn(includedColumns, inputs[0].length);
		double[][] inputsF = filterColumns(inputs, oldColumn2newColumn, includedColumns.cardinality());

		//check the input
		{
			if (inputs[0].length + 1 > inputs.length) {
				//not enough data
				message = "not enough traces for the number of parameters";
				return null;
			}
		}

		//perform the regression
		OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
		regression.newSampleData(outputs, inputsF);
		try {
			double[] result = regression.estimateRegressionParameters();

			getQualityMetrics().add(Pair.of("residual sum of squares", regression.calculateResidualSumOfSquares()));
			getQualityMetrics().add(Pair.of("regression standard error", regression.estimateRegressionStandardError()));

			return transformToOldColumns(result, oldColumn2newColumn, inputs[0].length);
		} catch (SingularMatrixException e) {
			message = "matrix is singular";
			return null;
		}

	}

	private static double[] transformToOldColumns(double[] array, int[] oldColumn2newColumn, int numberOfOldColumns) {
		double[] result = new double[numberOfOldColumns];
		for (int oldColumn = 0; oldColumn < numberOfOldColumns; oldColumn++) {
			int newColumn = oldColumn2newColumn[oldColumn];
			if (newColumn < 0) {
				result[oldColumn] = 0;
			} else {
				result[oldColumn] = array[newColumn];
			}
		}
		return result;
	}

	private static double[][] filterColumns(double[][] inputs, int[] oldColumn2newColumn, int numberOfNewColumns) {
		double[][] result = new double[inputs.length][numberOfNewColumns];
		for (int row = 0; row < result.length; row++) {
			for (int oldColumn = 0; oldColumn < inputs[row].length; oldColumn++) {
				int newColumn = oldColumn2newColumn[oldColumn];
				if (newColumn >= 0) {
					result[row][newColumn] = inputs[row][oldColumn];
				}
			}
		}
		return result;
	}

	private static BitSet findNonConstantColumns(double[][] array) {
		BitSet result = new BitSet();
		double[] value = array[0].clone();
		for (double[] arr : array) {
			for (int column = 0; column < arr.length; column++) {
				if (arr[column] != value[column]) {
					result.set(column);
				}
			}
		}
		return result;
	}

	private static BitSet findUniqueColumns(double[][] array) {
		BitSet result = new BitSet();
		for (int columnA = 0; columnA < array[0].length; columnA++) {
			if (!result.get(columnA)) {
				for (int columnB = columnA + 1; columnB < array[0].length; columnB++) {
					if (!result.get(columnB)) {
						if (columnsAreEquivalent(array, columnA, columnB)) {
							result.set(columnB);
						}
					}
				}
			}
		}

		result.flip(0, array[0].length);
		return result;
	}

	private static boolean columnsAreEquivalent(double[][] array, int columnA, int columnB) {
		for (int row = 0; row < array.length; row++) {
			if (array[row][columnA] != array[row][columnB]) {
				return false;
			}
		}
		return true;
	}

	private static int[] getOldColumn2newColumn(BitSet nonZeroColumns, int numberOfColumns) {
		int[] oldColumn2newColumn = new int[numberOfColumns];
		int newColumn = 0;
		for (int oldColumn = 0; oldColumn < numberOfColumns; oldColumn++) {
			if (nonZeroColumns.get(oldColumn)) {
				oldColumn2newColumn[oldColumn] = newColumn;
				newColumn++;
			} else {
				oldColumn2newColumn[oldColumn] = -1;
			}
		}
		return oldColumn2newColumn;
	}

	public String getMessage() {
		return message;
	}

	public List<Pair<String, Double>> getQualityMetrics() {
		return qualityMetrics;
	}

}