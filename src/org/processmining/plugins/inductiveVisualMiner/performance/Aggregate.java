package org.processmining.plugins.inductiveVisualMiner.performance;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

public enum Aggregate {
	minimum {
		public long finalise(long[] values) {
			if (values == null || values.length == 0) {
				return Long.MIN_VALUE;
			}
			long result = Long.MAX_VALUE;
			for (long value : values) {
				result = Math.min(result, value);
			}
			return result;
		}

		public String toString() {
			return "minimum";
		}
	},
	average {
		public long finalise(long values[]) {
			if (values == null || values.length == 0) {
				return Long.MIN_VALUE;
			}

			BigInteger sum = BigInteger.ZERO;
			for (long value : values) {
				sum = sum.add(BigInteger.valueOf(value));
			}

			return new BigDecimal(sum).divide(new BigDecimal(values.length), RoundingMode.HALF_UP).longValue();
		}
	},
	maximum {
		public long finalise(long[] values) {
			if (values == null || values.length == 0) {
				return Long.MIN_VALUE;
			}
			long result = Long.MIN_VALUE;
			for (long value : values) {
				result = Math.max(result, value);
			}
			return result;
		}

		public String toString() {
			return "maximum";
		}
	};

	/**
	 * 
	 * @param values
	 *
	 * @return The aggregated value, or Long.MIN_VALUE if that does not exist.
	 */
	public abstract long finalise(long[] values);
}