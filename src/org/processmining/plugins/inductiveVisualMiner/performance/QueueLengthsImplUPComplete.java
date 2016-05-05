package org.processmining.plugins.inductiveVisualMiner.performance;


public class QueueLengthsImplUPComplete extends QueueLengths {

	public double getQueueProbability(int unode, QueueActivityLog l, long time, int traceIndex) {
		if (l.getInitiate(traceIndex) > 0 && l.getComplete(traceIndex) > 0 && l.getInitiate(traceIndex) <= time
				&& time <= l.getComplete(traceIndex)) {
			return 1/3.0;
		}
		return 0;
	}
	
	public String getName() {
		return "UP complete";
	}

}
