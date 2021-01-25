package org.processmining.plugins.inductiveVisualMiner.performance;

import org.processmining.plugins.InductiveMiner.Sextuple;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IteratorWithPosition;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTraceImpl.ActivityInstanceIterator;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

public class QueueMineActivityLog {

	public static TIntObjectMap<QueueActivityLog> mine(IvMModel model, IvMLogFiltered tLog) {
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
		Long startTrace = Long.MAX_VALUE;
		Long endTrace = Long.MIN_VALUE;
		for (IvMMove move : tTrace) {
			if (move.getLogTimestamp() != null) {
				startTrace = Math.min(startTrace, move.getLogTimestamp());
				endTrace = Math.max(endTrace, move.getLogTimestamp());
			}
		}
		
		if (startTrace == Long.MAX_VALUE) {
			startTrace = null;
		}
		if (endTrace == Long.MIN_VALUE) {
			endTrace = null;
		}

		while (it.hasNext()) {
			Sextuple<Integer, String, IvMMove, IvMMove, IvMMove, IvMMove> activityInstance = it.next();

			if (activityInstance != null) {

				int node = activityInstance.getA();

				Long initiate = null;
				Long enqueue = null;
				Long start = null;
				Long complete = null;
				IvMMove initiateMove = null;
				IvMMove enqueueMove = null;
				IvMMove startMove = null;
				IvMMove completeMove = null;

				if (activityInstance.getC() != null) {
					initiate = activityInstance.getC().getLogTimestamp();
					initiateMove = activityInstance.getC();
				}
				if (activityInstance.getD() != null) {
					enqueue = activityInstance.getD().getLogTimestamp();
					enqueueMove = activityInstance.getD();
				}
				if (activityInstance.getE() != null) {
					start = activityInstance.getE().getLogTimestamp();
					startMove = activityInstance.getE();
				}
				if (activityInstance.getF() != null) {
					complete = activityInstance.getF().getLogTimestamp();
					completeMove = activityInstance.getF();
				}

				//put this activity instance in its list
				if (!timestamps.containsKey(node)) {
					timestamps.put(node, new QueueActivityLog());
				}

				timestamps.get(node).add(activityInstance.getB(), startTrace, initiate, initiateMove, enqueue,
						enqueueMove, start, startMove, complete, completeMove, endTrace, traceIndex);
			}
		}
	}
}
