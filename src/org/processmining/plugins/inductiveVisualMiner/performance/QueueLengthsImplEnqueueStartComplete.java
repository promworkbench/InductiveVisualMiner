package org.processmining.plugins.inductiveVisualMiner.performance;

import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public class QueueLengthsImplEnqueueStartComplete extends QueueLengths {

	public double getQueueProbability(UnfoldedNode unode, QueueActivityLog l, long time, int traceIndex) {
		return (l.getEnqueue(traceIndex) > 0 && l.getStart(traceIndex) > 0 && l.getEnqueue(traceIndex) <= time && time <= l
				.getStart(traceIndex)) ? 1 : 0;
	}
}
