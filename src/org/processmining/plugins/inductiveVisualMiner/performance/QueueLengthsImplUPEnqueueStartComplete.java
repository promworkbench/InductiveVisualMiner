package org.processmining.plugins.inductiveVisualMiner.performance;


public class QueueLengthsImplUPEnqueueStartComplete extends QueueLengths {

	public double getQueueProbability(int unode, QueueActivityLog l, long time, int traceIndex) {
		return (l.getEnqueue(traceIndex) > 0 && l.getStart(traceIndex) > 0 && l.getEnqueue(traceIndex) <= time && time <= l
				.getStart(traceIndex)) ? 1 : 0;
	}
	
	public String getName() {
		return "UP enqueue start complete";
	}
}
