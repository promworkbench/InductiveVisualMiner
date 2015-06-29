package org.processmining.plugins.inductiveVisualMiner.performance;

import gnu.trove.map.TObjectDoubleMap;

import java.util.Map;

import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public class QueueLengthsWrapper {

	private final Map<UnfoldedNode, QueueActivityLog> queueActivityLogs;
	private final TObjectDoubleMap<UnfoldedNode> sojournTimes;
	private final QueueLengths lengths;

	public QueueLengthsWrapper(QueueLengths lengths, Map<UnfoldedNode, QueueActivityLog> queueActivityLogs,
			TObjectDoubleMap<UnfoldedNode> sojournTimes) {
		this.lengths = lengths;
		this.sojournTimes = sojournTimes;
		this.queueActivityLogs = queueActivityLogs;
	}

	public double getQueueLength(UnfoldedNode unode, long time) {
		return lengths.getQueueLength(unode, time, queueActivityLogs);
	}

	public double getSojournTime(UnfoldedNode unode) {
		return sojournTimes.get(unode);
	}

	public TObjectDoubleMap<UnfoldedNode> getSojournTimes() {
		return sojournTimes;
	}

}
