package org.processmining.plugins.inductiveVisualMiner.performance;

import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;

import java.util.Map;

import org.processmining.ptconversions.pn.ProcessTree2Petrinet.UnfoldedNode;

public class QueueLengthsImplPHEnqueueStartComplete extends QueueLengths {

	private final TObjectDoubleMap<UnfoldedNode> lambdas;

	public QueueLengthsImplPHEnqueueStartComplete(Map<UnfoldedNode, QueueActivityLog> queueActivityLogs) {
		lambdas = new TObjectDoubleHashMap<>();
		for (UnfoldedNode unode : queueActivityLogs.keySet()) {
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

	public double getQueueProbability(UnfoldedNode unode, QueueActivityLog l, long time, int traceIndex) {
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
