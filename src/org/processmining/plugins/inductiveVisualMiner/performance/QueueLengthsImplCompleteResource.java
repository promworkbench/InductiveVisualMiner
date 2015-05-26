package org.processmining.plugins.inductiveVisualMiner.performance;

import gnu.trove.set.hash.THashSet;

import java.util.Map;
import java.util.Set;

import org.processmining.plugins.inductiveVisualMiner.animation.IvMLog;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public class QueueLengthsImplCompleteResource implements QueueLengths {

	private final Map<UnfoldedNode, QueueActivityLog> queueActivityLogs;

	public QueueLengthsImplCompleteResource(IvMLog iLog) {
		queueActivityLogs = QueueMineActivityLog.mine(iLog, true, false, false, true);
	}

	public double getQueueLength(UnfoldedNode unode, long time) {
		QueueActivityLog l = queueActivityLogs.get(unode);
		if (l == null) {
			return -1;
		}
		Set<String> resources = new THashSet<>();
		long count = 0;
		for (int index = 0; index < l.size(); index++) {
			if (l.getInitiate(index) <= time && time <= l.getComplete(index)) {
				//this activity instance is now in this activity
				count++;
				resources.add(l.getResource(index));
			}
		}
		return count - resources.size();
	}

}
