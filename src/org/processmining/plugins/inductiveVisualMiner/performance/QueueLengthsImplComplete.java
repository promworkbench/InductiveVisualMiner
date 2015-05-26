package org.processmining.plugins.inductiveVisualMiner.performance;

import java.util.Map;

import javax.swing.JOptionPane;

import org.processmining.plugins.inductiveVisualMiner.animation.IvMLog;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public class QueueLengthsImplComplete implements QueueLengths {

	private final Map<UnfoldedNode, QueueActivityLog> queueActivityLogs;
	private final int resources;

	public QueueLengthsImplComplete(IvMLog iLog) {
		queueActivityLogs = QueueMineActivityLog.mine(iLog, true, false, false, true);
		resources = Integer.valueOf((String) JOptionPane.showInputDialog(null, "Number of resources", "Resources",
				JOptionPane.PLAIN_MESSAGE, null, null, ""));
	}

	public double getQueueLength(UnfoldedNode unode, long time) {
		QueueActivityLog l = queueActivityLogs.get(unode);
		if (l == null) {
			return -1;
		}
		long count = 0;
		for (int index = 0; index < l.size(); index++) {
			if (l.getInitiate(index) <= time && time <= l.getComplete(index)) {
				//this activity instance is now in this activity
				count++;
			}
		}
		return Math.max(0, count - resources);
	}

}
