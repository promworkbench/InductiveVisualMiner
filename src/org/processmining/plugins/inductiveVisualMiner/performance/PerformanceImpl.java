package org.processmining.plugins.inductiveVisualMiner.performance;

import java.util.Arrays;

import org.processmining.plugins.InductiveMiner.Pair;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TObjectLongHashMap;

public class PerformanceImpl implements Performance {

	private THashMap<Pair<DurationType, Aggregate>, long[]> nodeMeasures = new THashMap<>();
	private TObjectLongHashMap<Pair<DurationType, Aggregate>> processMeasures = new TObjectLongHashMap<>(10, -.5f, -1);
	private final QueueLengths lengths;
	private final TIntObjectMap<QueueActivityLog> queueActivityLogs;

	public PerformanceImpl(QueueLengths lengths, TIntObjectMap<QueueActivityLog> queueActivityLogs,
			int maxNumberOfNodes) {
		this.lengths = lengths;
		this.queueActivityLogs = queueActivityLogs;

		//initialise node measures
		for (DurationType type : DurationType.values()) {
			for (Aggregate gather : Aggregate.values()) {
				long[] arr = new long[maxNumberOfNodes];
				Arrays.fill(arr, -1);
				nodeMeasures.put(Pair.of(type, gather), arr);
			}
		}
	}

	@Override
	public long getNodeMeasure(DurationType type, Aggregate gather, int node) {
		long[] arr = nodeMeasures.get(Pair.of(type, gather));
		if (arr == null) {
			return -1;
		}
		if (node < 0 || node > arr.length - 1) {
			return -1;
		}
		return arr[node];
	}

	public void setNodeMeasure(DurationType durationType, Aggregate gather, int node, long value) {
		nodeMeasures.get(Pair.of(durationType, gather))[node] = value;
	}

	@Override
	public long[] getNodeMeasures(DurationType durationType, Aggregate gather) {
		return nodeMeasures.get(Pair.of(durationType, gather));
	}

	@Override
	public double getQueueLength(int node, long time) {
		return lengths.getQueueLength(node, time, queueActivityLogs);
	}

	@Override
	public long getProcessMeasure(DurationType type, Aggregate gather) {
		return processMeasures.get(Pair.of(type, gather));
	}

	public void setProcessMeasure(DurationType durationType, Aggregate gather, long value) {
		processMeasures.put(Pair.of(durationType, gather), value);
	}

}