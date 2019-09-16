package org.processmining.plugins.inductiveVisualMiner.dataanalysis;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Correlation {
	public static BigDecimal correlation(double[] valuesX, double[] valuesY) {

		if (valuesX.length == 0) {
			return BigDecimal.valueOf(Double.MIN_VALUE);
		}

		BigDecimal meanX = mean(valuesX);
		BigDecimal standardDeviationX = new BigDecimal(standardDeviation(valuesX, meanX));
		BigDecimal meanY = mean(valuesY);
		BigDecimal standardDeviationY = new BigDecimal(standardDeviation(valuesY, meanY));

		if (standardDeviationX.equals(BigDecimal.ZERO) || standardDeviationY.equals(BigDecimal.ZERO)) {
			return BigDecimal.valueOf(Double.MIN_VALUE);
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
		BigDecimal sum = BigDecimal.ZERO;
		for (double value : values) {
			sum = sum.add(BigDecimal.valueOf(value));
		}
		return sum.divide(BigDecimal.valueOf(values.length), 10, RoundingMode.HALF_UP);
	}

	public static double standardDeviation(double[] values, BigDecimal mean) {
		BigDecimal sum = BigDecimal.ZERO;
		for (double value : values) {
			BigDecimal p = BigDecimal.valueOf(value).subtract(mean).pow(2);
			sum = sum.add(p);
		}
		BigDecimal d = sum.divide(BigDecimal.valueOf(values.length - 1), 10, RoundingMode.HALF_UP);

		return Math.sqrt(d.doubleValue());
	}
}
