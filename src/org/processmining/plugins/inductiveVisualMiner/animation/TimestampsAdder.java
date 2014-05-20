package org.processmining.plugins.inductiveVisualMiner.animation;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeTimestampImpl;
import org.processmining.plugins.InductiveMiner.mining.IMTraceG;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedLog;
import org.processmining.plugins.inductiveVisualMiner.alignment.Move;

public class TimestampsAdder {

	public static TimedTrace timeTrace(HashMap<List<XEventClass>, IMTraceG<Move>> map, XTrace trace, XLogInfo xLogInfo,
			double logMin, double logMax) {
		//create an aligned trace to search for
		List<XEventClass> lTrace = new ArrayList<>();
		for (XEvent event : trace) {
			lTrace.add(xLogInfo.getEventClasses().getClassOf(event));
		}
		if (map.containsKey(lTrace)) {
			//the trace was not filtered out
			IMTraceG<Move> alignedTrace = map.get(lTrace);

			//attach timestamps
			TimedTrace timedTrace = new TimedTrace();
			int xEventIndex = 0;
			double t = 0;
			for (Move move : alignedTrace) {
				if (move.eventClass != null) {
					XEvent event = trace.get(xEventIndex);
					xEventIndex++;
					
					Double t1 = TimestampsAdder.scale(TimestampsAdder.getTimestamp(event), logMin, logMax);
					if (t1 != null) {
					
						//perform sanity check
						if (t1 < t) {
							//this trace is not ordered according to timestamps
							System.out.println("Trace not ordered according to timestamps.");
							return null;
						}
						
						t = t1;
						timedTrace.add(new TimedMove(move, t));
	
						if (timedTrace.getStartTime() == 0) {
							timedTrace.setStartTime(t - 3);
						}
					} else {
						timedTrace.add(new TimedMove(move, null));
					}
				} else {
					timedTrace.add(new TimedMove(move, null));
				}
			}

			timedTrace.setEndTime(t + 3);
			return timedTrace;
		} else {
			//the trace was filtered out in an earlier step; do not translate
			return null;
		}
	}

	/*
	 * Make a log-projection hashmap
	 */
	public static HashMap<List<XEventClass>, IMTraceG<Move>> getIMTrace2AlignedTrace(AlignedLog aLog) {
		HashMap<List<XEventClass>, IMTraceG<Move>> result = new HashMap<>();
		for (IMTraceG<Move> aTrace : aLog) {
			List<XEventClass> trace = new ArrayList<>();
			for (Move m : aTrace) {
				if (m.eventClass != null) {
					trace.add(m.eventClass);
				}
			}
			result.put(trace, aTrace);
		}
		return result;
	}

	public static Double scale(Double value, double logMin, double logMax) {
		if (value == null) {
			return null;
		}
		double duration = 180;
		return 1 + duration * (value - logMin) / (logMax - logMin);
	}

	public static Double getTimestamp(XEvent event) {
		if (event.hasAttributes() && event.getAttributes().containsKey("time:timestamp")) {
			Date timestamp = ((XAttributeTimestampImpl) event.getAttributes().get("time:timestamp")).getValue();
			return (double) timestamp.getTime();
		}
		return null;
	}
}
