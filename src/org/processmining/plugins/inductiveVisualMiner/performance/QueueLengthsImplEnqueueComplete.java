package org.processmining.plugins.inductiveVisualMiner.performance;

import java.util.Map;

import javax.swing.JOptionPane;

import org.processmining.plugins.inductiveVisualMiner.animation.IvMLog;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public class QueueLengthsImplEnqueueComplete implements QueueLengths {

	private final Map<UnfoldedNode, QueueActivityLog> queueActivityLogs;
	private final float m1;
	private final float m2;

	public QueueLengthsImplEnqueueComplete(IvMLog tLog) {
		queueActivityLogs = QueueMineActivityLog.mine(tLog, false, true, false, true);

		//ask for numbers
		m1 = Float.valueOf((String) JOptionPane.showInputDialog(null, "m1", "Enqueue mean (m1)",
				JOptionPane.PLAIN_MESSAGE, null, null, ""));
		m2 = Float.valueOf((String) JOptionPane.showInputDialog(null, "m2", "Start mean (m2)",
				JOptionPane.PLAIN_MESSAGE, null, null, ""));
		
	}

	public double getQueueLength(UnfoldedNode unode, long time) {
		QueueActivityLog l = queueActivityLogs.get(unode);
		if (l == null) {
			return -1;
		}
		long count = 0;
		for (int index = 0; index < l.size(); index++) {
			if (l.getEnqueue(index) <= time && time <= l.getComplete(index)) {
				//this activity instance is now in this activity
				
				long timeInActivity = time - l.getEnqueue(index);
				if (timeInActivity > ((m1 + m2) / 2.0)) {
					//activity instance is past the average queue length
					count++;
				}
			}
		}
		return count;
	}

}
