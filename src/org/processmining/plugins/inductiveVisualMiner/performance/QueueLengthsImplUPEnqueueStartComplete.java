package org.processmining.plugins.inductiveVisualMiner.performance;

import org.processmining.ptconversions.pn.ProcessTree2Petrinet.UnfoldedNode;

public class QueueLengthsImplUPEnqueueStartComplete extends QueueLengths {

	public double getQueueProbability(UnfoldedNode unode, QueueActivityLog l, long time, int traceIndex) {
		return (l.getEnqueue(traceIndex) > 0 && l.getStart(traceIndex) > 0 && l.getEnqueue(traceIndex) <= time && time <= l
				.getStart(traceIndex)) ? 1 : 0;
	}
	
	public String getName() {
		return "UP enqueue start complete";
	}
}
