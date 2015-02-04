package org.processmining.plugins.inductiveVisualMiner.animation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import nl.tue.astar.AStarThread.Canceller;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.model.XEvent;
import org.processmining.plugins.InductiveMiner.mining.IMTrace;
import org.processmining.plugins.InductiveMiner.mining.IMTraceG;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog2;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedLog;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedTrace;
import org.processmining.plugins.inductiveVisualMiner.alignment.Move;

public class ComputeTimedLog {

	public static TimedLog computeTimedLog(final AlignedLog aLog, final IMLog2 log, final XLogInfo xLogInfo,
			final Canceller canceller) {

		//make a log-projection-hashmap
		HashMap<List<XEventClass>, AlignedTrace> map = TimestampsAdder.getIMTrace2AlignedTrace(aLog);

		TimedLog timedLog = new TimedLog();
		for (IMTrace trace : log) {
			TimedTrace tTrace = timeTrace(log, map, trace, xLogInfo);
			if (tTrace != null) {
				timedLog.add(tTrace);
			}
			if (canceller.isCancelled()) {
				return null;
			}
		}
		return timedLog;
	}

	private static TimedTrace timeTrace(IMLog2 log, HashMap<List<XEventClass>, AlignedTrace> map, IMTrace trace, XLogInfo xLogInfo) {

		//find the corresponding aligned trace
		List<XEventClass> lTrace = TimestampsAdder.getTraceLogProjection(trace, xLogInfo);
		IMTraceG<Move> alignedTrace = map.get(lTrace);
		if (alignedTrace == null) {
			return null;
		}

		//construct a timed trace
		String name;
		if (trace.getAttributes().containsKey("concept:name")) {
			name = trace.getAttributes().get("concept:name").toString();
		} else {
			name = "";
		}
		TimedTrace timedTrace = new TimedTrace(name);
		Iterator<XEvent> itEvent = trace.iterator();
		double lastSeenTimestamp = 0;
		for (Move move : alignedTrace) {
			if (move.getEventClass() != null) {

				XEvent event = itEvent.next();
				Long timestamp = TimestampsAdder.getTimestamp(event);

				//see if this event has a valid timestamp
				if (timestamp != null && timestamp >= lastSeenTimestamp) {
					lastSeenTimestamp = timestamp;
					timedTrace.add(new TimedMove(move, timestamp, log.isStart(event)));
				} else {
					timedTrace.add(new TimedMove(move, null, log.isComplete(event)));
				}
			} else {
				//model move or tau
				timedTrace.add(new TimedMove(move, null, false));
			}
		}

		return timedTrace;
	}
}
