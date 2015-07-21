package org.processmining.plugins.inductiveVisualMiner.ivmlog;

import gnu.trove.map.hash.THashMap;

import java.util.Iterator;
import java.util.List;

import nl.tue.astar.AStarThread.Canceller;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.model.XEvent;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedTrace;
import org.processmining.plugins.inductiveVisualMiner.alignment.Move;
import org.processmining.plugins.inductiveVisualMiner.animation.TimestampsAdder;

public class Alignment2IvMLog {

	public static IvMLogBase convert(final MultiSet<AlignedTrace> aLog, final IMLog log, final XEventClasses performanceEventClasses,
			final Canceller canceller) {

		//make a log-projection-hashmap
		THashMap<List<XEventClass>, AlignedTrace> map = TimestampsAdder.getIMTrace2AlignedTrace(aLog);

		IvMLogBase timedLog = new IvMLogBase();
		for (IMTrace trace : log) {
			IvMTrace tTrace = timeTrace(log, map, trace, performanceEventClasses);
			if (tTrace != null) {
				timedLog.add(tTrace);
			}
			if (canceller.isCancelled()) {
				return null;
			}
		}
		return timedLog;
	}

	private static IvMTrace timeTrace(IMLog log, THashMap<List<XEventClass>, AlignedTrace> map, IMTrace trace, XEventClasses performanceEventClasses) {

		//find the corresponding aligned trace
		List<XEventClass> lTrace = TimestampsAdder.getTraceLogProjection(trace, performanceEventClasses);
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
		IvMTrace timedTrace = new IvMTrace(name, trace.getAttributes());
		Iterator<XEvent> itEvent = trace.iterator();
		double lastSeenTimestamp = 0;
		for (Move move : alignedTrace) {
			if (move.isTauStart()) {
				//tau-start
				timedTrace.add(new IvMMove(move, null, null, null));
			} else if (move.getActivityEventClass() != null) {
				//sync move or log move

				XEvent event = itEvent.next();
				Long timestamp = TimestampsAdder.getTimestamp(event);
				
				String resource = ResourceFunctions.getResource(event);

				//see if this event has a valid timestamp
				if (timestamp != null && timestamp >= lastSeenTimestamp) {
					lastSeenTimestamp = timestamp;
				}
				timedTrace.add(new IvMMove(move, timestamp, resource, event.getAttributes()));
			} else {
				//model move or tau
				timedTrace.add(new IvMMove(move, null, null, null));
			}
		}

		return timedTrace;
	}
}
