package org.processmining.plugins.inductiveVisualMiner.performance;

import java.util.Map;

import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public abstract class QueueLengths {

	/**
	 * @param unode
	 * @param time
	 *            , use new Date(time) to make a Date-object
	 * @return the number of cases in queue for this unode
	 */
	public double getQueueLength(UnfoldedNode unode, long time, Map<UnfoldedNode, QueueActivityLog> queueActivityLogs) {
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
	public abstract double getQueueProbability(UnfoldedNode unode, QueueActivityLog l, long time, int traceIndex);
	
	public abstract String getName();
}
