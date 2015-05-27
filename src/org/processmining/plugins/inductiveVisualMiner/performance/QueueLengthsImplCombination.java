package org.processmining.plugins.inductiveVisualMiner.performance;

import org.processmining.plugins.inductiveVisualMiner.animation.IvMLog;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public class QueueLengthsImplCombination implements QueueLengths {

	private final QueueLengths qEstimate;
	private final QueueLengthsImplEnqueueStartComplete qReal;

	public QueueLengthsImplCombination(IvMLog iLog) {
		
//		new QueueLengthsImplOutput(iLog);
		
		qEstimate = new QueueLengthsImplPHComplete(iLog);
		qReal = new QueueLengthsImplEnqueueStartComplete(iLog);
		
		new QueueLengthsImplOutput(iLog);

		for (UnfoldedNode unode : qReal.queueActivityLogs.keySet()) {

			//find min and max
			long min = Long.MAX_VALUE;
			long max = Long.MIN_VALUE;
			QueueActivityLog l = qReal.queueActivityLogs.get(unode);
			for (int i = 0; i < l.size(); i++) {
				min = Math.min(min, l.getEnqueue(i));
				max = Math.max(max, l.getStart(i));
			}

			//compute mean square error
			double mse = RMSE.rmse(unode, qReal, qEstimate, min, max);
			System.out.println("RMSE for activity " + unode + ": " + mse);
		}
	}

	public double getQueueLength(UnfoldedNode unode, long time) {
		return qReal.getQueueLength(unode, time);
	}
}
