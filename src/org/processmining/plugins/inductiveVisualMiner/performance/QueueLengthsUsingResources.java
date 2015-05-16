package org.processmining.plugins.inductiveVisualMiner.performance;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

import java.util.Set;

import org.processmining.plugins.InductiveMiner.Sextuple;
import org.processmining.plugins.inductiveVisualMiner.animation.IvMLog;
import org.processmining.plugins.inductiveVisualMiner.animation.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.animation.IvMTrace;
import org.processmining.plugins.inductiveVisualMiner.animation.IvMTrace.ActivityInstanceIterator;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public class QueueLengthsUsingResources implements QueueLengths {

	THashMap<UnfoldedNode, QueueActivityLog> timestamps;

	public QueueLengthsUsingResources(ProcessTree tree, IvMLog tLog) {
		timestamps = new THashMap<>();
		init(tLog);
	}

	private void init(IvMLog tLog) {
		for (IvMTrace tTrace : tLog) {
			init(tTrace);
		}
	}

	/**
	 * Gather all activity instances
	 * 
	 * @param tTrace
	 */
	private void init(IvMTrace tTrace) {
		ActivityInstanceIterator it = tTrace.activityInstanceIterator();
		while (it.hasNext()) {
			Sextuple<UnfoldedNode, String, IvMMove, IvMMove, IvMMove, IvMMove> activityInstance = it.next();

			if (activityInstance != null) {

				//we are only interested in activity instances where both completes are known
				if (activityInstance.getC() != null && activityInstance.getF() != null
						&& activityInstance.getC().getLogTimestamp() != null
						&& activityInstance.getF().getLogTimestamp() != null) {

					Long initiate = null;
					Long enqueue = null;
					Long start = null;
					Long complete = null;

					if (activityInstance.getC() != null) {
						initiate = activityInstance.getC().getLogTimestamp();
					}
					if (activityInstance.getD() != null) {
						enqueue = activityInstance.getD().getLogTimestamp();
					}
					if (activityInstance.getE() != null) {
						start = activityInstance.getE().getLogTimestamp();
					}
					if (activityInstance.getF() != null) {
						complete = activityInstance.getF().getLogTimestamp();
					}

					//put this activity instance in its list
					if (!timestamps.containsKey(activityInstance.getA())) {
						timestamps.put(activityInstance.getA(), new QueueActivityLog());
					}

					timestamps.get(activityInstance.getA()).add(activityInstance.getB(), initiate, enqueue, start,
							complete);
				}
			}
		}
	}

	public long getQueueLength(UnfoldedNode unode, long time) {
		QueueActivityLog l = timestamps.get(unode);
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
