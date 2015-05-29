package org.processmining.plugins.inductiveVisualMiner.performance;

import java.util.Map;

import javax.swing.JOptionPane;

import org.jblas.DoubleMatrix;
import org.jblas.MatrixFunctions;
import org.processmining.plugins.inductiveVisualMiner.animation.IvMLog;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public class QueueLengthsImplPHStartComplete implements QueueLengths {

	private final Map<UnfoldedNode, QueueActivityLog> queueActivityLogs;

	private final double lambda1;
	private final double lambda2;

	public QueueLengthsImplPHStartComplete(IvMLog iLog) {
		queueActivityLogs = QueueMineActivityLog.mine(iLog, true, false, true, false);
		
		lambda1 = Double.valueOf((String) JOptionPane.showInputDialog(null, "Lambda", "Lambda 1",
				JOptionPane.PLAIN_MESSAGE, null, null, ""));
		lambda2 = Double.valueOf((String) JOptionPane.showInputDialog(null, "Lambda", "Lambda 2",
				JOptionPane.PLAIN_MESSAGE, null, null, ""));
	}

	public double getQueueLength(UnfoldedNode unode, long time) {
		QueueActivityLog l = queueActivityLogs.get(unode);
		if (l == null) {
			return -1;
		}
		
		double queueLength = 0;
		for (int index = 0; index < l.size(); index++) {
			if (l.getInitiate(index) <= time && time <= l.getStart(index)) {

				long xI = time - l.getInitiate(index);
		
				DoubleMatrix m = DoubleMatrix.zeros(2, 2);
				m.put(0, 0, (-lambda1) * xI);
				m.put(0, 1, lambda1 * xI);
				m.put(1,  1, (-lambda2) * xI);
				DoubleMatrix m2 = MatrixFunctions.expm(m);
				
				double p = m2.get(0, 1);
				
				queueLength += p;
			}
		}
		return queueLength;
	}
}
