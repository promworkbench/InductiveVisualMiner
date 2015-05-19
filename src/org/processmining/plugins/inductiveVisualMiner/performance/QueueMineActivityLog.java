package org.processmining.plugins.inductiveVisualMiner.performance;

import gnu.trove.map.hash.THashMap;

import java.util.Map;

import org.processmining.plugins.InductiveMiner.Sextuple;
import org.processmining.plugins.inductiveVisualMiner.animation.IvMLog;
import org.processmining.plugins.inductiveVisualMiner.animation.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.animation.IvMTrace;
import org.processmining.plugins.inductiveVisualMiner.animation.IvMTrace.ActivityInstanceIterator;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public class QueueMineActivityLog {

	public static Map<UnfoldedNode, QueueActivityLog> mine(IvMLog tLog, boolean requireInitiate,
			boolean requireEnqueue, boolean requireStart, boolean requireComplete) {
		Map<UnfoldedNode, QueueActivityLog> queueActivityLogs = new THashMap<>();
		for (IvMTrace tTrace : tLog) {
			mine(tTrace, queueActivityLogs, requireInitiate, requireEnqueue, requireStart, requireComplete);
		}
		return queueActivityLogs;
	}

	public static void mine(IvMTrace tTrace, Map<UnfoldedNode, QueueActivityLog> timestamps, boolean requireInitiate,
			boolean requireEnqueue, boolean requireStart, boolean requireComplete) {
		ActivityInstanceIterator it = tTrace.activityInstanceIterator();
		while (it.hasNext()) {
			Sextuple<UnfoldedNode, String, IvMMove, IvMMove, IvMMove, IvMMove> activityInstance = it.next();

			if (activityInstance != null) {

				//we are only interested in activity instances according to the parameters
				if (((activityInstance.getC() != null && activityInstance.getC().getLogTimestamp() != null) || !requireInitiate)
						&& ((activityInstance.getD() != null && activityInstance.getD().getLogTimestamp() != null) || !requireEnqueue)
						&& ((activityInstance.getE() != null && activityInstance.getE().getLogTimestamp() != null) || !requireStart)
						&& ((activityInstance.getF() != null && activityInstance.getF().getLogTimestamp() != null) || !requireComplete)) {

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
}
