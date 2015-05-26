package org.processmining.plugins.inductiveVisualMiner.performance;

import java.util.Map;

import javax.swing.JOptionPane;

import org.processmining.plugins.inductiveVisualMiner.animation.IvMLog;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public class QueueLengthsImplStartComplete implements QueueLengths {

	private final Map<UnfoldedNode, QueueActivityLog> queueActivityLogs;
	private final float m1;
	private final float m2;

	public QueueLengthsImplStartComplete(IvMLog tLog) {
		queueActivityLogs = QueueMineActivityLog.mine(tLog, true, false, true, false);

		//ask for numbers
		m1 = Float.valueOf((String) JOptionPane.showInputDialog(null, "m1", "Provide the transit mean (m1)",
				JOptionPane.PLAIN_MESSAGE, null, null, ""));
		m2 = Float.valueOf((String) JOptionPane.showInputDialog(null, "m2", "Provide the queue mean (m2)",
				JOptionPane.PLAIN_MESSAGE, null, null, ""));
		
	}

	public double getQueueLength(UnfoldedNode unode, long time) {
		QueueActivityLog l = queueActivityLogs.get(unode);
		if (l == null) {
			return -1;
		}
		long count = 0;
		for (int index = 0; index < l.size(); index++) {
			if (l.getInitiate(index) <= time && time <= l.getStart(index)) {
				//this activity instance is now in this activity
				
				long timeInActivity = time - l.getInitiate(index);
				if (timeInActivity > ((m1 + m2) / 2.0)) {
					//activity instance is past the average queue length
					count++;
				}
			}
		}
		return count;
	}

}
