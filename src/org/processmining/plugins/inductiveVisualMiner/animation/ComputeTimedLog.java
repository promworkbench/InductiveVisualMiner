package org.processmining.plugins.inductiveVisualMiner.animation;

import gnu.trove.map.hash.THashMap;

import java.util.Iterator;
import java.util.List;

import nl.tue.astar.AStarThread.Canceller;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.model.XEvent;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedLog;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedTrace;
import org.processmining.plugins.inductiveVisualMiner.alignment.Move;
import org.processmining.plugins.inductiveVisualMiner.resourcePerspective.ResourceFunctions;

public class ComputeTimedLog {

	public static TimedLog computeTimedLog(final AlignedLog aLog, final IMLog log, final XLogInfo xLogInfoPerformance,
			final Canceller canceller) {

		//make a log-projection-hashmap
		THashMap<List<XEventClass>, AlignedTrace> map = TimestampsAdder.getIMTrace2AlignedTrace(aLog);

		TimedLog timedLog = new TimedLog();
		for (IMTrace trace : log) {
			TimedTrace tTrace = timeTrace(log, map, trace, xLogInfoPerformance);
			if (tTrace != null) {
				timedLog.add(tTrace);
			}
			if (canceller.isCancelled()) {
				return null;
			}
		}
		return timedLog;
	}

	private static TimedTrace timeTrace(IMLog log, THashMap<List<XEventClass>, AlignedTrace> map, IMTrace trace, XLogInfo xLogInfoPerformance) {

		//find the corresponding aligned trace
		List<XEventClass> lTrace = TimestampsAdder.getTraceLogProjection(trace, xLogInfoPerformance);
		AlignedTrace alignedTrace = map.get(lTrace);
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
			if (move.isTauStart()) {
				//tau-start
				timedTrace.add(new TimedMove(move, null, null));
			} else if (move.getActivityEventClass() != null) {
				//sync move or log move

				XEvent event = itEvent.next();
				Long timestamp = TimestampsAdder.getTimestamp(event);
				
				String resource = ResourceFunctions.getResource(event);

				//see if this event has a valid timestamp
				if (timestamp != null && timestamp >= lastSeenTimestamp) {
					lastSeenTimestamp = timestamp;
				}
				timedTrace.add(new TimedMove(move, timestamp, resource));
			} else {
				//model move or tau
				timedTrace.add(new TimedMove(move, null, null));
			}
		}

		return timedTrace;
	}
}
