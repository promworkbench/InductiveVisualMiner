package org.processmining.plugins.inductiveVisualMiner.performance;

import org.jblas.DoubleMatrix;
import org.jblas.MatrixFunctions;

public class QueueLengthsImplPHComplete extends QueueLengths {

	private final double lambda1;
	private final double lambda2;
	private final double lambda3;

	public QueueLengthsImplPHComplete() {
		//L1
//		lambda1 = 0.000018868556;
//		lambda2 = 0.000004658797;
//		lambda3 = 0.000018671884;

		//L2
		//		lambda1 = 0.000004593318;
		//		lambda2 = 0.000115025201;
		//		lambda3 = 0.000027511080;

		//L3
				lambda1 = 0.0000006178375;
				lambda2 = 0.0000006053810;
				lambda3 = 0.0000006078836;

	}

	public double getQueueProbability(int unode, QueueActivityLog l, long time, int traceIndex) {
		if (l.getInitiate(traceIndex) > 0 && l.getComplete(traceIndex) > 0 && l.getInitiate(traceIndex) <= time
				&& time <= l.getComplete(traceIndex)) {

			long xI = time - l.getInitiate(traceIndex);

			DoubleMatrix m = DoubleMatrix.zeros(3, 3);
			m.put(0, 0, (-lambda1) * xI);
			m.put(0, 1, lambda1 * xI);
			m.put(1, 1, (-lambda2) * xI);
			m.put(1, 2, lambda2 * xI);
			m.put(2, 2, (-lambda3) * xI);
			DoubleMatrix m2 = MatrixFunctions.expm(m);

			return m2.get(0, 1);
		}
		return 0;
	}

	public String getName() {
		return "PH complete";
	}
}
