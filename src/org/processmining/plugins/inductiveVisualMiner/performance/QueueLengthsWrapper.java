package org.processmining.plugins.inductiveVisualMiner.performance;

import java.util.Map;

import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public class QueueLengthsWrapper {
	
	private final Map<UnfoldedNode, QueueActivityLog> queueActivityLogs;
	private final QueueLengths lengths;
	
	public QueueLengthsWrapper(QueueLengths lengths, Map<UnfoldedNode, QueueActivityLog> queueActivityLogs) {
		this.lengths = lengths;
		this.queueActivityLogs = queueActivityLogs;
	}
	
	public double getQueueLength(UnfoldedNode unode, long time) {
		return lengths.getQueueLength(unode, time, queueActivityLogs);
	}
	
}
