package org.processmining.plugins.inductiveVisualMiner.performance;

import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;


public interface QueueLengths {
	
	/**
	 * @param unode
	 * @param time, use new Date(time) to make a Date-object
	 * @return the number of cases in queue for this unode
	 */
	public long getQueueLength(UnfoldedNode unode, long time);
}
