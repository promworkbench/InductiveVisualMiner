package org.processmining.plugins.inductiveVisualMiner.performance;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.EnumSet;

import org.processmining.plugins.inductiveVisualMiner.dataanalysis.traceattributes.Correlation;

public enum Aggregate {
	@AggregateLevel
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
	@AggregateLevel
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
	
	@AggregateLevel(showInPopup = false)
	median {
		public long finalise(long[] values) {
			if (values == null || values.length == 0) {
				return Long.MIN_VALUE;
			}
			long result = (long) Correlation.median(values);
			return result;
		}

		public String toString() {
			return "median";
		}
	},
	
	@AggregateLevel
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

	// cache values
	private static Aggregate[] valuesForPopups;
	static {
		EnumSet<Aggregate> valuesForPopup = EnumSet.noneOf(Aggregate.class);
		for (Aggregate aggregate : Aggregate.values()) {
			try {
				AggregateLevel[] annotations = aggregate.getClass().getField(aggregate.name())
						.getAnnotationsByType(AggregateLevel.class);
				for (AggregateLevel annotation : annotations) {
					if (annotation.showInPopup()) {
						valuesForPopup.add(aggregate);
					}
				}
			} catch (NoSuchFieldException | SecurityException e) {
				e.printStackTrace();
			}
		}
		valuesForPopups = new Aggregate[valuesForPopup.size()];
		valuesForPopups = valuesForPopup.toArray(valuesForPopups);
	}

	public static Aggregate[] valuesForPopups() {
		return valuesForPopups;
	}
}