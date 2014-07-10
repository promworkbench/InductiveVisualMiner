package org.processmining.plugins.inductiveVisualMiner.animation;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.model.XAttributeTimestamp;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.InductiveMiner.mining.IMTraceG;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedLog;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedTrace;
import org.processmining.plugins.inductiveVisualMiner.alignment.Move;

public class TimestampsAdder {

	public static double animationDuration = 20;
	public static double beginEndEdgeDuration = 1;
	private static Random random = new Random(123);

//	public static TimedTrace timeTrace(HashMap<List<XEventClass>, IMTraceG<Move>> map, XTrace trace, XLogInfo xLogInfo,
//			Pair<Double, Double> extremeTimestamps, boolean showDeviations, ShortestPathGraph shortestGraph,
//			AlignedLogVisualisationInfo info, Scaler scaler) {
//
//		//create an aligned trace to search for
//		List<XEventClass> lTrace = getTraceLogProjection(trace, xLogInfo);
//
//		if (map.containsKey(lTrace)) {
//			//the trace was not filtered out
//			IMTraceG<Move> alignedTrace = map.get(lTrace);
//
//			TimedTrace timedTrace;
//			if (extremeTimestamps != null) {
//				timedTrace = timeTraceFromTimestamps(alignedTrace, trace, xLogInfo, extremeTimestamps, showDeviations,
//						info);
//
//				//set start end and time of trace
//				timedTrace.setStartTime(guessStartTime(timedTrace, shortestGraph, info, scaler));
//				timedTrace.setEndTime(guessEndTime(timedTrace, timedTrace.getStartTime(), shortestGraph, info, scaler));
//			} else {
//				timedTrace = timeTraceDummyTimestamps(alignedTrace, trace, xLogInfo, showDeviations);
//			}
//
//			if (timedTrace == null) {
//				return null;
//			}
//
//			return timedTrace;
//		} else {
//			return null;
//		}
//	}

//	private static TimedTrace timeTraceDummyTimestamps(IMTraceG<Move> alignedTrace, XTrace trace, XLogInfo xLogInfo,
//			boolean showDeviations) {
//
//		//attach timestamps
//		TimedTrace timedTrace = new TimedTrace();
//
//		timedTrace.setStartTime(randomStartTime());
//		double traceDuration = randomDuration(timedTrace.getStartTime());
//		timedTrace.setEndTime(timedTrace.getStartTime() + traceDuration);
//
//		for (Move move : alignedTrace) {
//			if (showDeviations || move.getType() != Type.log) {
//				timedTrace.add(new TimedMove(move, null, null));
//			}
//		}
//
//		return timedTrace;
//	}

//	private static double randomDuration(double startTime) {
//		double traceDuration = 10 + random.nextInt((int) (animationDuration - (startTime + 10)));
//		return traceDuration;
//	}
//
//	private static double randomStartTime() {
//		double startTime;
//		startTime = random.nextInt((int) (animationDuration - 10));
//		return startTime;
//	}

//	private static TimedTrace timeTraceFromTimestamps(IMTraceG<Move> alignedTrace, XTrace trace, XLogInfo xLogInfo,
//			Pair<Double, Double> extremeTimestamps, boolean showDeviations, AlignedLogVisualisationInfo info) {
//
//		//attach timestamps
//		TimedTrace timedTrace = new TimedTrace();
//		int xEventIndex = 0;
//		double t = 0;
//		for (Move move : alignedTrace) {
//			if (move.getEventClass() != null) {
//
//				//filter out log moves if wanted
//				if (showDeviations || move.isModelSync()) {
//
//					XEvent event = trace.get(xEventIndex);
//					xEventIndex++;
//
//					Double t1 = TimestampsAdder.scale(TimestampsAdder.getTimestamp(event), extremeTimestamps);
//					if (t1 != null) {
//
//						//perform sanity check
//						if (t1 < t) {
//							//this trace is not ordered according to timestamps
//							System.out.println("Trace not ordered according to timestamps.");
//							return null;
//						}
//
//						t = t1;
//						timedTrace.add(new TimedMove(move, t, TimestampsAdder.getTimestamp(event)));
//					} else {
//						timedTrace.add(new TimedMove(move, null, null));
//					}
//				}
//			} else {
//				timedTrace.add(new TimedMove(move, null, null));
//			}
//		}
//
//		return timedTrace;
//	}

	public static List<XEventClass> getTraceLogProjection(XTrace trace, XLogInfo xLogInfo) {
		List<XEventClass> lTrace = new ArrayList<>();
		for (XEvent event : trace) {
			lTrace.add(xLogInfo.getEventClasses().getClassOf(event));
		}
		return lTrace;
	}

	/*
	 * Make a log-projection hashmap
	 */
	public static HashMap<List<XEventClass>, IMTraceG<Move>> getIMTrace2AlignedTrace(AlignedLog aLog) {
		HashMap<List<XEventClass>, IMTraceG<Move>> result = new HashMap<>();
		for (AlignedTrace aTrace : aLog) {
			List<XEventClass> trace = new ArrayList<>();
			for (Move m : aTrace) {
				if (m.getEventClass() != null) {
					trace.add(m.getEventClass());
				}
			}
			result.put(trace, aTrace);
		}
		return result;
	}

//	public static double guessStartTime(List<TimedMove> trace, ShortestPathGraph shortestGraph,
//			AlignedLogVisualisationInfo info, Scaler scaler) {
//		//find the first timed move
//		TimedMove firstTimedMove = null;
//		int firstTimedMoveIndex;
//		for (firstTimedMoveIndex = 0; firstTimedMoveIndex < trace.size(); firstTimedMoveIndex++) {
//			firstTimedMove = trace.get(firstTimedMoveIndex);
//			if (firstTimedMove.getScaledTimestamp(scaler) != null) {
//				break;
//			}
//		}
//
//		if (firstTimedMove == null) {
//			return randomStartTime();
//		}
//
//		//find the edges the trace is going through before the first timed move
//		List<TimedMove> partialTrace;
//		try {
//			partialTrace = trace.subList(0, firstTimedMoveIndex + 1);
//		} catch (Exception e) {
//			throw e;
//		}
//
//		//the trace ends with 2 seconds per edge
//		return firstTimedMove.getScaledTimestamp(scaler)
//				- Animation.getEdgesOnMovePath(partialTrace, shortestGraph, info, true, false).size()
//				* beginEndEdgeDuration;
//	}

//	public static double guessEndTime(List<TimedMove> trace, double startTime, ShortestPathGraph shortestGraph,
//			AlignedLogVisualisationInfo info, Scaler scaler) {
//		//find the last timed move
//		TimedMove lastTimedMove = null;
//		int lastTimedMoveIndex;
//		for (lastTimedMoveIndex = trace.size() - 1; lastTimedMoveIndex >= 0; lastTimedMoveIndex--) {
//			lastTimedMove = trace.get(lastTimedMoveIndex);
//			if (lastTimedMove.getScaledTimestamp(scaler) != null) {
//				break;
//			}
//		}
//
//		if (lastTimedMove == null) {
//			return randomDuration(startTime);
//		}
//
//		//find the edges the trace is going through after the last timed move
//		List<TimedMove> partialTrace = trace.subList(lastTimedMoveIndex, trace.size());
//
//		//the trace ends with 2 seconds per edge
//		return lastTimedMove.getScaledTimestamp(scaler)
//				+ Animation.getEdgesOnMovePath(partialTrace, shortestGraph, info, false, true).size()
//				* beginEndEdgeDuration;
//	}

//	public static Double scale(Long value, Pair<Double, Double> extremeTimestamps) {
//		if (value == null) {
//			return null;
//		}
//		return animationDuration * (value - extremeTimestamps.getLeft())
//				/ (extremeTimestamps.getRight() - extremeTimestamps.getLeft());
//	}

	public static Long getTimestamp(XEvent event) {
		if (event.hasAttributes() && event.getAttributes().containsKey("time:timestamp")) {
			Date timestamp = ((XAttributeTimestamp) event.getAttributes().get("time:timestamp")).getValue();
			return (long) timestamp.getTime();
		}
		return null;
	}
}
