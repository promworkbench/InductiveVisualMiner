package org.processmining.plugins.inductiveVisualMiner.performance;

import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.hash.THashSet;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import org.processmining.plugins.inductiveVisualMiner.animation.IvMLog;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public class QueueLengthsImplComplete implements QueueLengths {

	private final Map<UnfoldedNode, QueueActivityLog> queueActivityLogs;
	private final TObjectIntHashMap<UnfoldedNode> node2resources;

	public QueueLengthsImplComplete(IvMLog tLog) {
		queueActivityLogs = QueueMineActivityLog.mine(tLog, true, false, false, true);

		//for each activity, find the number of resources that is working on it
		//assumption: this number is constant
		//assumption: there is at least one busy period, i.e. where all resources are serving at the same time
		node2resources = new TObjectIntHashMap<>();
		for (UnfoldedNode unode : queueActivityLogs.keySet()) {
			QueueActivityLog l = queueActivityLogs.get(unode);

			//make a sorted list of events
			//use a trick: hide the fact whether it's a start or complete in the long
			long[] list = new long[l.size() * 2];
			for (int index = 0; index < l.size(); index++) {
				list[index * 2] = Long.MIN_VALUE + l.getInitiate(index) * 2;
				list[index * 2 + 1] = Long.MIN_VALUE + l.getComplete(index) * 2 + 1;
			}
			Arrays.sort(list);

			//now we walk through the list and keep track of the number of resources that is busy 			
			int count = 0;
			int minCount = 0;
			for (int i = 0; i < list.length; i++) {
				if (list[i] % 2 == 0) {
					//initiate event
					System.out.println("initiate");
					count++;
				} else {
					//complete event
					System.out.println("complete");
					count--;
				}
			}
		}
		throw new RuntimeException("Not yet implemented");
	}

	public long getQueueLength(UnfoldedNode unode, long time) {
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
