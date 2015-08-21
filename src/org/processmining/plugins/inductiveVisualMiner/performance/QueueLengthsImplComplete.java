package org.processmining.plugins.inductiveVisualMiner.performance;

import java.util.Map;

import javax.swing.JOptionPane;

import org.processmining.ptconversions.pn.ProcessTree2Petrinet.UnfoldedNode;

public class QueueLengthsImplComplete extends QueueLengths {

	private final int resources;

	public QueueLengthsImplComplete() {
		resources = Integer.valueOf((String) JOptionPane.showInputDialog(null, "Number of resources", "Resources",
				JOptionPane.PLAIN_MESSAGE, null, null, ""));
	}

	@Override
	public double getQueueLength(UnfoldedNode unode, long time, Map<UnfoldedNode, QueueActivityLog> queueActivityLogs) {
		QueueActivityLog l = queueActivityLogs.get(unode);
		if (l == null) {
			return -1;
		}
		long count = 0;
		for (int index = 0; index < l.size(); index++) {
			if (l.getInitiate(index) > 0 && l.getComplete(index) > 0 && l.getInitiate(index) <= time
					&& time <= l.getComplete(index)) {
				//this activity instance is now in this activity
				count++;
			}
		}
		return Math.max(0, count - resources);
	}

	public double getQueueProbability(UnfoldedNode unode, QueueActivityLog l, long time, int traceIndex) {
		throw new RuntimeException("You shouldn't arrive here.");
	}

	public String getName() {
		return "dumb counting complete";
	}
}
