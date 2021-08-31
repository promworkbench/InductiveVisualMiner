package org.processmining.plugins.inductiveVisualMiner.cost;

import java.util.BitSet;

import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;

public class Regression {

	public static double[] regress(double[][] inputs, double[] outputs) {
		if (inputs.length == 0) {
			return null;
		}

		//filter zero-columns
		BitSet nonZeroColumns = findNonConstantColumns(inputs);

		if (nonZeroColumns.isEmpty()) {
			return null;
		}

		int[] oldColumn2newColumn = getOldColumn2newColumn(nonZeroColumns, inputs[0].length);
		double[][] inputsF = filterColumns(inputs, oldColumn2newColumn, nonZeroColumns.cardinality());

		//perform the regression
		OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
		regression.newSampleData(outputs, inputsF);
		double[] result = regression.estimateRegressionParameters();

		return transformToOldColumns(result, oldColumn2newColumn, inputs[0].length);
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

}