package org.processmining.plugins.inductiveVisualMiner.performance;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

import gnu.trove.map.TIntObjectMap;

public class PerformanceWrapper {

	public enum TypeNode {
		elapsed, queueing, waiting, service, sojourn, remaining;
	}

	public enum TypeGlobal {
		cycle;
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
	private long[][][] valuesNode;
	private long[][] valuesGlobal;

	//intermediate values
	private BigInteger[][][] valuesINode;
	private BigInteger[][] valuesIGlobal;
	private int[][][] countINode;
	private int[][] countIGlobal;

	public PerformanceWrapper(QueueLengths lengths, TIntObjectMap<QueueActivityLog> queueActivityLogs,
			int numberOfNodes) {
		this.lengths = lengths;
		this.queueActivityLogs = queueActivityLogs;

		valuesINode = new BigInteger[TypeNode.values().length][Gather.values().length][numberOfNodes];
		valuesIGlobal = new BigInteger[TypeGlobal.values().length][Gather.values().length];
		countINode = new int[TypeNode.values().length][Gather.values().length][numberOfNodes];
		countIGlobal = new int[TypeGlobal.values().length][Gather.values().length];
	}

	public void addNodeValue(TypeNode type, int unode, long value) {
		int t = type.ordinal();
		for (Gather gather : Gather.values()) {
			int g = gather.ordinal();
			countINode[t][g][unode]++;
			valuesINode[t][g][unode] = gather.addValue(valuesINode[t][g][unode], value);
		}
	}

	public void addGlobalValue(TypeGlobal type, long value) {
		int t = type.ordinal();
		for (Gather gather : Gather.values()) {
			int g = gather.ordinal();
			countIGlobal[t][g]++;
			valuesIGlobal[t][g] = gather.addValue(valuesIGlobal[t][g], value);
		}
	}

	public void finalise() {
		valuesNode = new long[TypeNode.values().length][Gather.values().length][valuesINode[0][0].length];
		for (TypeNode type : TypeNode.values()) {
			for (Gather gather : Gather.values()) {
				int t = type.ordinal();
				int g = gather.ordinal();
				for (int unode = 0; unode < valuesNode[0][0].length; unode++) {
					if (countINode[t][g][unode] > 0) {
						valuesNode[t][g][unode] = gather.finalise(valuesINode[t][g][unode], countINode[t][g][unode]);
					} else {
						valuesNode[t][g][unode] = -1;
					}
				}
			}
		}

		valuesGlobal = new long[TypeGlobal.values().length][Gather.values().length];
		for (TypeGlobal type : TypeGlobal.values()) {
			for (Gather gather : Gather.values()) {
				int t = type.ordinal();
				int g = gather.ordinal();
				if (countIGlobal[t][g] > 0) {
					valuesGlobal[t][g] = gather.finalise(valuesIGlobal[t][g], countIGlobal[t][g]);
				} else {
					valuesGlobal[t][g] = -1;
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
	public long getNodeMeasure(TypeNode type, Gather gather, int unode) {
		return valuesNode[type.ordinal()][gather.ordinal()][unode];
	}

	public long[] getNodeMeasures(TypeNode type, Gather gather) {
		return valuesNode[type.ordinal()][gather.ordinal()];
	}

	public double getQueueLength(int unode, long time) {
		return lengths.getQueueLength(unode, time, queueActivityLogs);
	}

	public long getGlobalMeasure(TypeGlobal type, Gather gather) {
		return valuesGlobal[type.ordinal()][gather.ordinal()];
	}

}
