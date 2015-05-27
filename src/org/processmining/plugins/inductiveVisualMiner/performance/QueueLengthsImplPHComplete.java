package org.processmining.plugins.inductiveVisualMiner.performance;

import java.util.Map;

import org.jblas.DoubleMatrix;
import org.jblas.MatrixFunctions;
import org.processmining.plugins.inductiveVisualMiner.animation.IvMLog;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public class QueueLengthsImplPHComplete implements QueueLengths {

	private class Cluster implements Comparable<Cluster> {
		public int size;
		public double center;

		public int compareTo(Cluster arg0) {
			return Double.compare(center, arg0.center);
		}
	}

	private final Map<UnfoldedNode, QueueActivityLog> queueActivityLogs;

	private final static int k = 5;

	public QueueLengthsImplPHComplete(IvMLog iLog) {
		queueActivityLogs = QueueMineActivityLog.mine(iLog, true, false, false, true);
		for (UnfoldedNode unode : queueActivityLogs.keySet()) {
			QueueActivityLog l = queueActivityLogs.get(unode);

			double[][] ds = {{2.0, 1.0}, {1.0, 2.0}};
			DoubleMatrix m = new DoubleMatrix(ds);
			DoubleMatrix m2 = MatrixFunctions.expm(m);
			System.out.println(m2);
		}
	}

	public double getQueueLength(UnfoldedNode unode, long time) {
		return 0;
	}
}
