package org.processmining.plugins.inductiveVisualMiner.performance;

import gnu.trove.map.TIntDoubleMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntDoubleHashMap;

public class QueueLengthsImplPHEnqueueStartComplete extends QueueLengths {

	private final TIntDoubleMap lambdas;

	public QueueLengthsImplPHEnqueueStartComplete(TIntObjectMap<QueueActivityLog> queueActivityLogs) {
		lambdas = new TIntDoubleHashMap(10, 0.5f, -1, 0);
		for (int unode : queueActivityLogs.keySet().toArray()) {
			QueueActivityLog l = queueActivityLogs.get(unode);
			long sum = 0;
			long count = 0;
			for (int i = 0; i < l.size(); i++) {
				if (l.getStart(i) > 0 && l.getEnqueue(i) > 0) {
					sum += l.getStart(i) - l.getEnqueue(i);
					count++;
				}
			}
			lambdas.put(unode, 1 / (sum / (count * 1.0)));
		}
	}

	public double getQueueProbability(int unode, QueueActivityLog l, long time, int traceIndex) {
		if (l.getEnqueue(traceIndex) > 0 && l.getStart(traceIndex) > 0 && l.getEnqueue(traceIndex) <= time
				&& time <= l.getStart(traceIndex)) {
			double lambda = lambdas.get(unode);
			long xI = time - l.getEnqueue(traceIndex);
			return lambda * Math.exp(-lambda * xI);
		}
		return 0;
	}

	public String getName() {
		return "PH enqueue start complete";
	}
}
