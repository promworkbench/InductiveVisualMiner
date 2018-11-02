package org.processmining.plugins.inductiveVisualMiner.performance;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

import gnu.trove.map.TIntObjectMap;

public class PerformanceWrapper {

	public enum Type {
		queueing, waiting, service, sojourn;
	}

	public enum Gather {
		min {
			public BigInteger addValue(BigInteger oldValue, long value) {
				return oldValue == null ? BigInteger.valueOf(value) : oldValue.min(BigInteger.valueOf(value));
			}

			public long finalise(BigInteger value, int numberOfMeasures) {
				return value.longValue();
			}
			
			public String toString() {
				return "minimum";
			}
		},
		average {
			public BigInteger addValue(BigInteger oldValue, long value) {
				return oldValue == null ? BigInteger.valueOf(value) : oldValue.add(BigInteger.valueOf(value));
			}

			public long finalise(BigInteger value, int numberOfMeasures) {
				BigDecimal quotient = new BigDecimal(value).divide(new BigDecimal(numberOfMeasures),
						RoundingMode.HALF_UP);
				return quotient.longValue();
			}
		},
		max {
			public BigInteger addValue(BigInteger oldValue, long value) {
				return oldValue == null ? BigInteger.valueOf(value) : oldValue.max(BigInteger.valueOf(value));
			}

			public long finalise(BigInteger value, int numberOfMeasures) {
				return value.longValue();
			}
			
			public String toString() {
				return "maximum";
			}
		};

		public abstract BigInteger addValue(BigInteger oldValue, long value);

		public abstract long finalise(BigInteger value, int numberOfMeasures);
	}

	private final TIntObjectMap<QueueActivityLog> queueActivityLogs;
	private final QueueLengths lengths;

	//finalised values
	private long[][][] values;

	//intermediate values
	private BigInteger[][][] valuesI;
	private int[][][] countI;

	public PerformanceWrapper(QueueLengths lengths, TIntObjectMap<QueueActivityLog> queueActivityLogs,
			int numberOfNodes) {
		this.lengths = lengths;
		this.queueActivityLogs = queueActivityLogs;

		valuesI = new BigInteger[Type.values().length][Gather.values().length][numberOfNodes];
		countI = new int[Type.values().length][Gather.values().length][numberOfNodes];
	}

	public void addValue(Type type, int unode, long value) {
		int t = type.ordinal();
		for (Gather gather : Gather.values()) {
			int g = gather.ordinal();
			countI[t][g][unode]++;
			valuesI[t][g][unode] = gather.addValue(valuesI[t][g][unode], value);
		}
	}

	public void finalise() {
		values = new long[Type.values().length][Gather.values().length][valuesI[0][0].length];
		for (Type type : Type.values()) {
			for (Gather gather : Gather.values()) {
				int t = type.ordinal();
				int g = gather.ordinal();
				for (int unode = 0; unode < values[0][0].length; unode++) {
					if (countI[t][g][unode] > 0) {
						values[t][g][unode] = gather.finalise(valuesI[t][g][unode], countI[t][g][unode]);
					} else {
						values[t][g][unode] = -1;
					}
				}
			}
		}
	}

	/**
	 * Returns the asked measure, or -1 if it does not exist.
	 * 
	 * @param type
	 * @param gather
	 * @param unode
	 * @return
	 */
	public long getMeasure(Type type, Gather gather, int unode) {
		return values[type.ordinal()][gather.ordinal()][unode];
	}

	public long[] getMeasures(Type type, Gather gather) {
		return values[type.ordinal()][gather.ordinal()];
	}

	public double getQueueLength(int unode, long time) {
		return lengths.getQueueLength(unode, time, queueActivityLogs);
	}

}
