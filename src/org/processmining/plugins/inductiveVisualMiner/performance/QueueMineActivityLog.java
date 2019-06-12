package org.processmining.plugins.inductiveVisualMiner.performance;

import java.util.Collections;

import org.processmining.plugins.InductiveMiner.Sextuple;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IteratorWithPosition;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLog;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTraceImpl.ActivityInstanceIterator;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

public class QueueMineActivityLog {

	public static TIntObjectMap<QueueActivityLog> mine(IvMModel model, IvMLog tLog) {
		TIntObjectMap<QueueActivityLog> queueActivityLogs = new TIntObjectHashMap<QueueActivityLog>(10, 0.5f, -1);
		for (IteratorWithPosition<IvMTrace> it = tLog.iterator(); it.hasNext();) {
			IvMTrace tTrace = it.next();
			int traceIndex = it.getPosition();
			mine(model, tTrace, traceIndex, queueActivityLogs);
		}
		return queueActivityLogs;
	}

	public static void mine(IvMModel model, IvMTrace tTrace, int traceIndex,
			TIntObjectMap<QueueActivityLog> timestamps) {
		ActivityInstanceIterator it = tTrace.activityInstanceIterator(model);

		//find the start timestamp of the trace
		Long startTrace = null;
		for (IvMMove move : tTrace) {
			if (move.getLogTimestamp() != null) {
				startTrace = move.getLogTimestamp();
				break;
			}
		}

		//find the end timestamp of the trace
		Long endTrace = null;
		Collections.reverse(tTrace);
		for (IvMMove move : tTrace) {
			if (move.getLogTimestamp() != null) {
				endTrace = move.getLogTimestamp();
				break;
			}
		}
		Collections.reverse(tTrace);

		while (it.hasNext()) {
			Sextuple<Integer, String, IvMMove, IvMMove, IvMMove, IvMMove> activityInstance = it.next();

			if (activityInstance != null) {

				int node = activityInstance.getA();

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

				timestamps.get(node).add(activityInstance.getB(), startTrace, initiate, enqueue, start, complete,
						endTrace, traceIndex);
			}
		}
	}
}
