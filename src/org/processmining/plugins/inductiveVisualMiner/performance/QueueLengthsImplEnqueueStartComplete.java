package org.processmining.plugins.inductiveVisualMiner.performance;

import java.util.Map;

import org.processmining.plugins.inductiveVisualMiner.animation.IvMLog;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public class QueueLengthsImplEnqueueStartComplete implements QueueLengths {

	private final Map<UnfoldedNode, QueueActivityLog> queueActivityLogs;

	public QueueLengthsImplEnqueueStartComplete(IvMLog iLog) {
		queueActivityLogs = QueueMineActivityLog.mine(iLog, false, true, true, false);
	}

	public long getQueueLength(UnfoldedNode unode, long time) {
		QueueActivityLog l = queueActivityLogs.get(unode);
		if (l == null) {
			return -1;
		}
		long count = 0;
		for (int index = 0; index < l.size(); index++) {
			if (l.getEnqueue(index) <= time && time <= l.getStart(index)) {
				//this activity instance is now queueing
				count++;
			}
		}
		return count;
	}

}
