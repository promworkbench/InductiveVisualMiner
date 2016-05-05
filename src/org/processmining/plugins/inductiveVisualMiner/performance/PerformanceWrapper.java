package org.processmining.plugins.inductiveVisualMiner.performance;

import gnu.trove.map.TIntDoubleMap;
import gnu.trove.map.TIntObjectMap;

public class PerformanceWrapper {

	private final TIntObjectMap<QueueActivityLog> queueActivityLogs;
	private final TIntDoubleMap waitingTimes;
	private final TIntDoubleMap queueingTimes;
	private final TIntDoubleMap serviceTimes;
	private final TIntDoubleMap sojournTimes;
	private final QueueLengths lengths;

	public PerformanceWrapper(QueueLengths lengths, TIntObjectMap<QueueActivityLog> queueActivityLogs,
			TIntDoubleMap waitingTimes, TIntDoubleMap queueingTimes, TIntDoubleMap serviceTimes,
			TIntDoubleMap sojournTimes) {
		this.lengths = lengths;
		this.waitingTimes = waitingTimes;
		this.queueingTimes = queueingTimes;
		this.serviceTimes = serviceTimes;
		this.sojournTimes = sojournTimes;
		this.queueActivityLogs = queueActivityLogs;
	}

	public double getQueueLength(int unode, long time) {
		return lengths.getQueueLength(unode, time, queueActivityLogs);
	}

	public double getWaitingTime(int unode) {
		return waitingTimes.get(unode);
	}

	public double getQueueingTime(int unode) {
		return queueingTimes.get(unode);
	}

	public double getServiceTime(int unode) {
		return serviceTimes.get(unode);
	}

	public double getSojournTime(int unode) {
		return sojournTimes.get(unode);
	}

	public TIntDoubleMap getSojournTimes() {
		return sojournTimes;
	}

}
