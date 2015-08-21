package org.processmining.plugins.inductiveVisualMiner.performance;

import org.jblas.DoubleMatrix;
import org.jblas.MatrixFunctions;
import org.processmining.ptconversions.pn.ProcessTree2Petrinet.UnfoldedNode;

public class QueueLengthsImplPHStartComplete extends QueueLengths {

	private final double lambda1;
	private final double lambda2;

	public QueueLengthsImplPHStartComplete() {
		//L1
//		lambda1 = 0.00002382820;
//		lambda2 = 0.00001211212;
		
		//L2
		lambda1 = 0.0005964011;
		lambda2 = 0.0000154282;
				
		//L3
//		lambda1 = 0.0000011513989;
//		lambda2 = 0.0000007212469;
	}

	public double getQueueProbability(UnfoldedNode unode, QueueActivityLog l, long time, int traceIndex) {
		if (l.getInitiate(traceIndex) > 0 && l.getStart(traceIndex) > 0 && l.getInitiate(traceIndex) <= time
				&& time <= l.getStart(traceIndex)) {
			long xI = time - l.getInitiate(traceIndex);

			DoubleMatrix m = DoubleMatrix.zeros(2, 2);
			m.put(0, 0, (-lambda1) * xI);
			m.put(0, 1, lambda1 * xI);
			m.put(1, 1, (-lambda2) * xI);
			DoubleMatrix m2 = MatrixFunctions.expm(m);

			return m2.get(0, 1);
		}
		return 0;
	}
	
	public String getName() {
		return "PH start complete";
	}
}
