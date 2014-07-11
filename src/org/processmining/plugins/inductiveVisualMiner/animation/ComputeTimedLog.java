package org.processmining.plugins.inductiveVisualMiner.animation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import nl.tue.astar.AStarThread.Canceller;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.InductiveMiner.mining.IMTraceG;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedLog;
import org.processmining.plugins.inductiveVisualMiner.alignment.Move;

public class ComputeTimedLog {

	public static TimedLog computeTimedLog(final AlignedLog aLog, final XLog xLog, final XLogInfo xLogInfo,
			final Canceller canceller) {

		//make a log-projection-hashmap
		HashMap<List<XEventClass>, IMTraceG<Move>> map = TimestampsAdder.getIMTrace2AlignedTrace(aLog);

		TimedLog timedLog = new TimedLog();
		for (XTrace xTrace : xLog) {
			TimedTrace tTrace = timeTrace(map, xTrace, xLogInfo);
			if (tTrace != null) {
				timedLog.add(tTrace);
			}
			if (canceller.isCancelled()) {
				return null;
			}
		}
		return timedLog;
	}

	private static TimedTrace timeTrace(HashMap<List<XEventClass>, IMTraceG<Move>> map, XTrace trace, XLogInfo xLogInfo) {

		//find the corresponding aligned trace
		List<XEventClass> lTrace = TimestampsAdder.getTraceLogProjection(trace, xLogInfo);
		IMTraceG<Move> alignedTrace = map.get(lTrace);
		if (alignedTrace == null) {
			return null;
		}

		//construct a timed trace
		TimedTrace timedTrace = new TimedTrace();
		Iterator<XEvent> itEvent = trace.iterator();
		double lastSeenTimestamp = 0;
		for (Move move : alignedTrace) {
			if (move.getEventClass() != null) {

				Long timestamp = TimestampsAdder.getTimestamp(itEvent.next());

				//see if this event has a valid timestamp
				if (timestamp != null && timestamp >= lastSeenTimestamp) {
					lastSeenTimestamp = timestamp;
					timedTrace.add(new TimedMove(move, timestamp));
				} else {
					timedTrace.add(new TimedMove(move, null));
				}
			} else {
				//model move or tau
				timedTrace.add(new TimedMove(move, null));
			}
		}

		return timedTrace;
	}
}
