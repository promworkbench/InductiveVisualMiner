package org.processmining.plugins.inductiveVisualMiner.performance;

import java.util.Date;

import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public class QueueLengthsImplDummy implements QueueLengths {

	public double getQueueLength(UnfoldedNode unode, long time) {
		return (new Date()).getTime();
	}
	
}
