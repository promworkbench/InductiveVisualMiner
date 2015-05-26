package org.processmining.plugins.inductiveVisualMiner.performance;

import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public class RMSE {

	public static double rmse(UnfoldedNode unode, QueueLengthsImplEnqueueStartComplete real, QueueLengths method,
			long min, long max) {
		long sum = 0;
		long bias = 0;
		long count = 0;
		for (long t = min; t <= max; t += 60000) {
			long sumReal = 0;
			long sumMethod = 0;
			for (int s = 0; s < 60; s++) {
				sumReal += real.getQueueLength(unode, t);
				sumMethod += method.getQueueLength(unode, t);
			}
			sum += Math.pow((sumReal / 60.0) - (sumMethod / 60.0), 2);
			bias += (sumReal / 60.0) - (sumMethod / 60.0);
			count++;
		}

		System.out.println("bias " + bias / (count * 1.0));
		return Math.pow(sum / (count * 1.0), 0.5);
	}
}
