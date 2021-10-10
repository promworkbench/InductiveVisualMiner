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

		public String toString() {
			return "average";
		}
	},

	@AggregateLevel(showInPopup = false)
	standardDeviation {

		/**
		 * Adapted from
		 * https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Online_algorithm
		 */
		public long finalise(long[] values) {
			// For a new value newValue, compute the new count, new mean, the new M2.
			// mean accumulates the mean of the entire dataset
			// M2 aggregates the squared distance from the mean
			// count aggregates the number of samples seen so far
			int count = 0;
			BigDecimal mean = BigDecimal.ZERO;
			BigDecimal M2 = BigDecimal.ZERO;

			for (long value : values) {
				//			def update(existingAggregate, newValue):
				//			    (count, mean, M2) = existingAggregate
				//			    count += 1
				count++;
				//			    delta = newValue - mean
				BigDecimal delta = BigDecimal.valueOf(value).subtract(mean);
				//			    mean += delta / count
				mean = mean.add(delta.divide(BigDecimal.valueOf(count), 10, RoundingMode.HALF_EVEN));
				//			    delta2 = newValue - mean
				BigDecimal delta2 = BigDecimal.valueOf(value).subtract(mean);
				//			    M2 += delta * delta2
				M2 = M2.add(delta.multiply(delta2));
				//			    return (count, mean, M2)
			}

			// # Retrieve the mean, variance and sample variance from an aggregate

			//			def finalize(existingAggregate):
			//			    (count, mean, M2) = existingAggregate
			if (count < 2) {
				return Long.MIN_VALUE;
			} else {
				//			        (mean, variance, sampleVariance) = (mean, M2 / count, M2 / (count - 1));
				BigDecimal variance = M2.divide(BigDecimal.valueOf(count), 10, RoundingMode.HALF_UP);
				return Math.round(Math.sqrt(variance.longValue()));
			}
		}

		public String toString() {
			return "standard deviation";
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