package org.processmining.plugins.inductiveVisualMiner.performance;

import java.util.Date;

import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public class QueueLengthsImplDummy implements QueueLengths {

	public long getQueueLength(UnfoldedNode unode, long time) {
		return (new Date()).getTime();
	}
	
}
