package org.processmining.plugins.inductiveVisualMiner.performance;

import gnu.trove.map.TObjectDoubleMap;

import java.util.Map;

import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public class PerformanceWrapper {

	private final Map<UnfoldedNode, QueueActivityLog> queueActivityLogs;
	private final TObjectDoubleMap<UnfoldedNode> waitingTimes;
	private final TObjectDoubleMap<UnfoldedNode> serviceTimes;
	private final TObjectDoubleMap<UnfoldedNode> sojournTimes;
	private final QueueLengths lengths;

	public PerformanceWrapper(QueueLengths lengths, Map<UnfoldedNode, QueueActivityLog> queueActivityLogs,
			TObjectDoubleMap<UnfoldedNode> waitingTimes, TObjectDoubleMap<UnfoldedNode> serviceTimes,
			TObjectDoubleMap<UnfoldedNode> sojournTimes) {
		this.lengths = lengths;
		this.waitingTimes = waitingTimes;
		this.serviceTimes = serviceTimes;
		this.sojournTimes = sojournTimes;
		this.queueActivityLogs = queueActivityLogs;
	}

	public double getQueueLength(UnfoldedNode unode, long time) {
		return lengths.getQueueLength(unode, time, queueActivityLogs);
	}

	public double getWaitingTime(UnfoldedNode unode) {
		return waitingTimes.get(unode);
	}

	public double getServiceTime(UnfoldedNode unode) {
		return serviceTimes.get(unode);
	}

	public double getSojournTime(UnfoldedNode unode) {
		return sojournTimes.get(unode);
	}

	public TObjectDoubleMap<UnfoldedNode> getSojournTimes() {
		return sojournTimes;
	}

}
