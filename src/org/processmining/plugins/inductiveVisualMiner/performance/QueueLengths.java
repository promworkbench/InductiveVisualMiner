package org.processmining.plugins.inductiveVisualMiner.performance;

import gnu.trove.map.TIntObjectMap;

public abstract class QueueLengths {

	/**
	 * @param unode
	 * @param time
	 *            , use new Date(time) to make a Date-object
	 * @return the number of cases in queue for this unode
	 */
	public double getQueueLength(int unode, long time, TIntObjectMap<QueueActivityLog> queueActivityLogs) {
		QueueActivityLog l = queueActivityLogs.get(unode);
		if (l == null) {
			return -1;
		}

		double queueLength = 0;
		for (int index = 0; index < l.size(); index++) {
			queueLength += getQueueProbability(unode, l, time, index);
		}
		return queueLength;
	}

	/**
	 * 
	 * @param unode
	 * @param l
	 * @param time
	 * @param traceIndex
	 * @return the probability that the trace is in queue at this moment.
	 */
	public abstract double getQueueProbability(int unode, QueueActivityLog l, long time, int traceIndex);

	public abstract String getName();
}
