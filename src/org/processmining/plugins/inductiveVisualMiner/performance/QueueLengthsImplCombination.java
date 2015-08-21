package org.processmining.plugins.inductiveVisualMiner.performance;

import java.util.Map;

import org.processmining.ptconversions.pn.ProcessTree2Petrinet.UnfoldedNode;

public class QueueLengthsImplCombination extends QueueLengths {
	
	public final QueueLengths initiateComplete;
	public final QueueLengths initiateStart;
	public final QueueLengths enqueueComplete;
	public final QueueLengths enqueueStart;
	
	public QueueLengthsImplCombination(Map<UnfoldedNode, QueueActivityLog> queueActivityLogs) {
		initiateComplete = new QueueLengthsImplCLIComplete(queueActivityLogs, 3);
		initiateStart = new QueueLengthsImplCLIStartComplete(queueActivityLogs, 2);
		enqueueComplete = new QueueLengthsImplCLIComplete(queueActivityLogs, 3);
		enqueueStart = new QueueLengthsImplUPEnqueueStartComplete();
	}

	public double getQueueProbability(UnfoldedNode unode, QueueActivityLog l, long time, int traceIndex) {
		if (l.getInitiate(traceIndex) > 0) {
			//initiate timestamp
			if (l.getComplete(traceIndex) > 0) {
				//initiate + complete
				return initiateComplete.getQueueProbability(unode, l, time, traceIndex);
			} else {
				//initiate + start
				return initiateStart.getQueueProbability(unode, l, time, traceIndex);
			}
		} else {
			//enqueue timestamp
			if (l.getComplete(traceIndex) > 0) {
				//enqueue + complete
				return enqueueComplete.getQueueProbability(unode, l, time, traceIndex);
			} else {
				//enqueue + start
				return enqueueStart.getQueueProbability(unode, l, time, traceIndex);
			}
		}
	}

	public String getName() {
		return "combination";
	}
	
}
