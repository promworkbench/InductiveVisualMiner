package org.processmining.plugins.inductiveVisualMiner.performance;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import org.processmining.plugins.InductiveMiner.Sextuple;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMEfficientTree;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLog;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTraceImpl.ActivityInstanceIterator;

public class QueueMineActivityLog {

	public static TIntObjectMap<QueueActivityLog> mine(IvMEfficientTree tree, IvMLog tLog) {
		TIntObjectMap<QueueActivityLog> queueActivityLogs = new TIntObjectHashMap<QueueActivityLog>(10, 0.5f, -1);
		for (IvMTrace tTrace : tLog) {
			mine(tree, tTrace, queueActivityLogs);
		}
		return queueActivityLogs;
	}

	public static void mine(IvMEfficientTree tree, IvMTrace tTrace, TIntObjectMap<QueueActivityLog> timestamps) {
		ActivityInstanceIterator it = tTrace.activityInstanceIterator(tree);
		while (it.hasNext()) {
			Sextuple<Integer, String, IvMMove, IvMMove, IvMMove, IvMMove> activityInstance = it.next();

			if (activityInstance != null) {

				int node = activityInstance.getA();

				//we are only interested in activity instances according to the parameters
				if (((activityInstance.getC() != null && activityInstance.getC().getLogTimestamp() != null) || (activityInstance
						.getD() != null && activityInstance.getD().getLogTimestamp() != null))
						&& ((activityInstance.getE() != null && activityInstance.getE().getLogTimestamp() != null) || (activityInstance
								.getF() != null && activityInstance.getF().getLogTimestamp() != null))) {

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
					if (!timestamps.containsKey(node)) {
						timestamps.put(node, new QueueActivityLog());
					}

					timestamps.get(node).add(activityInstance.getB(), initiate, enqueue, start, complete);
				}
			}
		}
	}
}
