package org.processmining.plugins.inductiveVisualMiner.performance;

import org.processmining.plugins.inductiveVisualMiner.animation.IvMLog;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public class QueueLengthsImplCombination implements QueueLengths {

//	private final QueueLengths qC;
	private final QueueLengths qCR;
//	private final QueueLengths qSC;
	private final QueueLengths qESC;

	public QueueLengthsImplCombination(IvMLog iLog) {
//		qC = new QueueLengthsImplComplete(iLog);
		qCR = new QueueLengthsImplCompleteResource(iLog);
//		qSC = new QueueLengthsImplStartComplete(iLog);
		qESC = new QueueLengthsImplEnqueueStartComplete(iLog);
	}

	public long getQueueLength(UnfoldedNode unode, long time) {
		System.out.println("queue @" + time);
//		System.out.println("complete 		       " + qC.getQueueLength(unode, time));
		System.out.println("complete resource      " + qCR.getQueueLength(unode, time));
//		System.out.println("start complete         " + qSC.getQueueLength(unode, time));
		System.out.println("enqueue start complete " + qESC.getQueueLength(unode, time));
		
		return qCR.getQueueLength(unode, time);
	}
}
