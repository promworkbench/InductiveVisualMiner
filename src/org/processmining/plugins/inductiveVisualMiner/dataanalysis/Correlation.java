package org.processmining.plugins.inductiveVisualMiner.dataanalysis;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.processmining.plugins.InductiveMiner.Pair;

import gnu.trove.list.array.TDoubleArrayList;

public class Correlation {

	public static Pair<double[], double[]> filterMissingValues(double[] valuesX, double[] valuesY) {
		TDoubleArrayList newValuesX = new TDoubleArrayList();
		TDoubleArrayList newValuesY = new TDoubleArrayList();

		for (int i = 0; i < valuesX.length; i++) {
			if (valuesX[i] > -Double.MAX_VALUE && valuesY[i] > -Double.MAX_VALUE) {
				newValuesX.add(valuesX[i]);
				newValuesY.add(valuesY[i]);
			}
		}

		return Pair.of(newValuesX.toArray(), newValuesY.toArray());
	}

	public static double median(double[] values) {
		if (values.length % 2 == 1) {
			return quickSelect(values, 0, values.length - 1, values.length / 2);
		} else {
			return (quickSelect(values, 0, values.length - 1, values.length / 2 - 1)
					+ quickSelect(values, 0, values.length - 1, values.length / 2)) / 2.0;
		}
	}

	public static double quickSelect(double[] arr, int left, int right, int k) {
		while (true) {
			if (k >= 0 && k <= right - left + 1) {
				int pos = randomPartition(arr, left, right);
				if (pos - left == k) {
					return arr[pos];
				}
				if (pos - left > k) {
					right = pos - 1;
					//return quickSelect(arr, left, pos - 1, k);
				} else {
					k = k - pos + left - 1;
					left = pos + 1;
					//return quickSelect(arr, pos + 1, right, k - pos + left - 1);
				}
			} else {
				return 0;
			}
		}
	}

	public static int partitionIterative(double[] arr, int left, int right) {
		double pivot = arr[right];
		int i = left;
		for (int j = left; j <= right - 1; j++) {
			if (arr[j] <= pivot) {
				swap(arr, i, j);
				i++;
			}
		}
		swap(arr, i, right);
		return i;
	}

	public static void swap(double[] arr, int n1, int n2) {
		double temp = arr[n2];
		arr[n2] = arr[n1];
		arr[n1] = temp;
	}

	public static int randomPartition(double[] arr, int left, int right) {
		int n = right - left + 1;
		int pivot = (int) (Math.random()) * n;
		swap(arr, left + pivot, right);
		return partitionIterative(arr, left, right);
	}

	public static BigDecimal correlation(double[] valuesX, double[] valuesY, BigDecimal meanY,
			double standardDeviationYd) {
		if (valuesX.length <= 1) {
			return BigDecimal.valueOf(-Double.MAX_VALUE);
		}

		BigDecimal meanX = mean(valuesX);
		BigDecimal standardDeviationX = new BigDecimal(standardDeviation(valuesX, meanX));
		BigDecimal standardDeviationY = BigDecimal.valueOf(standardDeviationYd);

		if (standardDeviationX.compareTo(BigDecimal.ZERO) == 0 || standardDeviationY.compareTo(BigDecimal.ZERO) == 0) {
			return BigDecimal.valueOf(-Double.MAX_VALUE);
		}

		BigDecimal sum = BigDecimal.ZERO;
		for (int i = 0; i < valuesX.length; i++) {
			BigDecimal x = BigDecimal.valueOf(valuesX[i]).subtract(meanX).divide(standardDeviationX, 10,
					RoundingMode.HALF_UP);
			BigDecimal y = BigDecimal.valueOf(valuesY[i]).subtract(meanY).divide(standardDeviationY, 10,
					RoundingMode.HALF_UP);
			sum = sum.add(x.multiply(y));
		}
		return sum.divide(BigDecimal.valueOf(valuesX.length - 1), 10, RoundingMode.HALF_UP);
	}

	public static BigDecimal mean(double[] values) {
		if (values.length > 0) {
			BigDecimal sum = BigDecimal.ZERO;
			for (double value : values) {
				sum = sum.add(BigDecimal.valueOf(value));
			}
			return sum.divide(BigDecimal.valueOf(values.length), 10, RoundingMode.HALF_UP);
		} else {
			return BigDecimal.ZERO;
		}
	}

	public static double standardDeviation(double[] values, BigDecimal mean) {
		if (values.length > 1) {
			BigDecimal sum = BigDecimal.ZERO;
			for (double value : values) {
				BigDecimal p = BigDecimal.valueOf(value).subtract(mean).pow(2);
				sum = sum.add(p);
			}
			BigDecimal d = sum.divide(BigDecimal.valueOf(values.length - 1), 10, RoundingMode.HALF_UP);

			return Math.sqrt(d.doubleValue());
		} else if (values.length == 1) {
			return 0;
		} else {
			return -Double.MAX_VALUE;
		}
	}
}
