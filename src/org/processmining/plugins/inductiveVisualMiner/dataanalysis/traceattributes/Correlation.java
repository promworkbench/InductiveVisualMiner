package org.processmining.plugins.inductiveVisualMiner.dataanalysis.traceattributes;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.processmining.plugins.InductiveMiner.Pair;

import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TLongArrayList;

public class Correlation {

	/**
	 * Only keeps the elements of which both lists have a value.
	 * 
	 * @param valuesX
	 * @param valuesY
	 * @return
	 */
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

	public static Pair<long[], double[]> filterMissingValues(long[] valuesX, double[] valuesY) {
		TLongArrayList newValuesX = new TLongArrayList();
		TDoubleArrayList newValuesY = new TDoubleArrayList();

		for (int i = 0; i < valuesX.length; i++) {
			if (valuesX[i] > Long.MIN_VALUE && valuesY[i] > -Double.MAX_VALUE) {
				newValuesX.add(valuesX[i]);
				newValuesY.add(valuesY[i]);
			}
		}

		return Pair.of(newValuesX.toArray(), newValuesY.toArray());
	}

	public static double median(double[] values) {
		values = values.clone();
		if (values.length % 2 == 1) {
			return quickSelect(values, 0, values.length - 1, values.length / 2);
		} else {
			return (quickSelect(values, 0, values.length - 1, values.length / 2 - 1)
					+ quickSelect(values, 0, values.length - 1, values.length / 2)) / 2.0;
		}
	}

	public static double median(long[] values) {
		values = values.clone();
		if (values.length % 2 == 1) {
			return quickSelect(values, 0, values.length - 1, values.length / 2);
		} else {
			return (quickSelect(values, 0, values.length - 1, values.length / 2 - 1)
					+ quickSelect(values, 0, values.length - 1, values.length / 2)) / 2.0;
		}
	}

	private static double quickSelect(double[] arr, int left, int right, int k) {
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

	private static long quickSelect(long[] arr, int left, int right, int k) {
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

	private static int partitionIterative(long[] arr, int left, int right) {
		long pivot = arr[right];
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

	private static void swap(double[] arr, int n1, int n2) {
		double temp = arr[n2];
		arr[n2] = arr[n1];
		arr[n1] = temp;
	}

	private static void swap(long[] arr, int n1, int n2) {
		long temp = arr[n2];
		arr[n2] = arr[n1];
		arr[n1] = temp;
	}

	private static int randomPartition(double[] arr, int left, int right) {
		int n = right - left + 1;
		int pivot = (int) (Math.random()) * n;
		swap(arr, left + pivot, right);
		return partitionIterative(arr, left, right);
	}

	private static int randomPartition(long[] arr, int left, int right) {
		int n = right - left + 1;
		int pivot = (int) (Math.random()) * n;
		swap(arr, left + pivot, right);
		return partitionIterative(arr, left, right);
	}

	/**
	 * 
	 * @param valuesX
	 * @param valuesY
	 * @param meanY
	 * @param standardDeviationYd
	 * @return the correlation, or BigDecimal.valueOf(-Double.MAX_VALUE) if it
	 *         does not exist
	 */
	public static BigDecimal correlation(double[] valuesX, double[] valuesY, BigDecimal meanY,
			double standardDeviationYd) {
		if (valuesX.length <= 1) {
			return BigDecimal.valueOf(-Double.MAX_VALUE);
		}

		BigDecimal meanX = mean(valuesX);
		if (meanX == null) {
			return BigDecimal.valueOf(-Double.MAX_VALUE);
		}
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

	/**
	 * 
	 * @param valuesX
	 * @param valuesY
	 * @param meanY
	 * @param standardDeviationYd
	 * @return correlation or BigDecimal.valueOf(-Double.MAX_VALUE) if it does
	 *         not exist
	 */
	public static BigDecimal correlation(double[] valuesX, long[] valuesY, BigDecimal meanY,
			double standardDeviationYd) {
		if (valuesX.length <= 1) {
			return BigDecimal.valueOf(-Double.MAX_VALUE);
		}

		BigDecimal meanX = mean(valuesX);
		if (meanX == null) {
			return BigDecimal.valueOf(-Double.MAX_VALUE);
		}
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

	/**
	 * 
	 * @param values
	 * @return the mean, or null if there is no average.
	 */
	public static BigDecimal mean(double[] values) {
		if (values.length > 0) {
			BigDecimal sum = BigDecimal.ZERO;
			int count = 0;
			for (double value : values) {
				if (Double.isInfinite(value)) {
					return null;
				}
				if (isValid(value)) {
					sum = sum.add(BigDecimal.valueOf(value));
					count++;
				}
			}
			if (count == 0) {
				return null;
			}
			return sum.divide(BigDecimal.valueOf(count), 10, RoundingMode.HALF_UP);
		} else {
			return null;
		}
	}

	public static BigDecimal mean(long[] values) {
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

	/**
	 * 
	 * @param values
	 * @param mean
	 * @return standard deviation, or -Double.MAX_VALUE if it does not exist
	 */
	public static double standardDeviation(double[] values, BigDecimal mean) {
		if (values.length > 1) {
			BigDecimal sum = BigDecimal.ZERO;
			int count = 0;
			for (double value : values) {
				if (Double.isInfinite(value)) {
					return -Double.MAX_VALUE;
				}
				if (isValid(value)) {
					BigDecimal p = BigDecimal.valueOf(value).subtract(mean).pow(2);
					sum = sum.add(p);
					count++;
				}
			}

			if (count <= 1) {
				return -Double.MAX_VALUE;
			}

			BigDecimal d = sum.divide(BigDecimal.valueOf(count - 1), 10, RoundingMode.HALF_UP);

			return Math.sqrt(d.doubleValue());
		} else if (values.length == 1) {
			return 0;
		} else {
			return -Double.MAX_VALUE;
		}
	}

	public static double standardDeviation(long[] values, BigDecimal mean) {
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

	public static boolean isValid(double value) {
		return value != -Double.MAX_VALUE && !Double.isNaN(value);
	}
}
