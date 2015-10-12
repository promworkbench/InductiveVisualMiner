package org.processmining.plugins.inductiveVisualMiner.performance;

import gnu.trove.set.hash.THashSet;

import java.util.Map;
import java.util.Set;

import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public class QueueLengthsImplCompleteResource extends QueueLengths {

	@Override
	public double getQueueLength(UnfoldedNode unode, long time, Map<UnfoldedNode, QueueActivityLog> queueActivityLogs) {
		QueueActivityLog l = queueActivityLogs.get(unode);
		if (l == null) {
			return -1;
		}
		Set<String> resources = new THashSet<>();
		long count = 0;
		for (int index = 0; index < l.size(); index++) {
			if (l.getInitiate(index) > 0 && l.getComplete(index) > 0 && l.getInitiate(index) <= time
					&& time <= l.getComplete(index)) {
				//this activity instance is now in this activity
				count++;
				resources.add(l.getResource(index));
			}
		}
		return count - resources.size();
	}

	public double getQueueProbability(UnfoldedNode unode, QueueActivityLog l, long time, int traceIndex) {
		throw new RuntimeException("You shouldn't arrive here.");
	}
	
	public String getName() {
		return "dumb counting complete resource";
	}
}
