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
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.mining.IMTraceG;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerPanel;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedLog;
import org.processmining.plugins.inductiveVisualMiner.alignment.Move;

public class TimestampsAdder {

	public static double animationDuration = 180;
	public static double beginEndEdgeDuration = 1;
	private static Random random = new Random(123);

	public static Pair<Double, Double> getExtremeTimestamps(XLog xLog, long maxTraces) {
		double logMin = Double.MAX_VALUE;
		double logMax = Double.MIN_VALUE;
		{
			long indexTrace = 0;
			for (XTrace trace : xLog) {

				if (indexTrace == maxTraces) {
					break;
				}

				for (XEvent event : trace) {
					if (event.getAttributes().containsKey("time:timestamp")) {
						double t = TimestampsAdder.getTimestamp(event);
						logMin = Math.min(logMin, t);
						logMax = Math.max(logMax, t);
					}
				}

				indexTrace++;
			}
		}
		if (logMin == Double.MAX_VALUE) {
			return null;
		}
		return Pair.of(logMin, logMax);
	}

	public static TimedTrace timeTrace(HashMap<List<XEventClass>, IMTraceG<Move>> map, XTrace trace, XLogInfo xLogInfo,
			Pair<Double, Double> extremeTimestamps, InductiveVisualMinerPanel panel) {

		//create an aligned trace to search for
		List<XEventClass> lTrace = getTraceLogProjection(trace, xLogInfo);

		if (map.containsKey(lTrace)) {
			//the trace was not filtered out
			IMTraceG<Move> alignedTrace = map.get(lTrace);

			TimedTrace timedTrace;
			if (extremeTimestamps != null) {
				timedTrace = timeTraceFromTimestamps(alignedTrace, trace, xLogInfo, extremeTimestamps, panel);
			} else {
				timedTrace = timeTraceDummyTimestamps(alignedTrace, trace, xLogInfo, panel);
			}
			
			if (timedTrace == null) {
				return null;
			}

			//set start end and time of trace
			timedTrace.setStartTime(guessStartTime(timedTrace, panel));
			timedTrace.setEndTime(guessEndTime(timedTrace, panel));

			return timedTrace;
		} else {
			return null;
		}
	}

	private static TimedTrace timeTraceDummyTimestamps(IMTraceG<Move> alignedTrace, XTrace trace, XLogInfo xLogInfo,
			InductiveVisualMinerPanel panel) {

		//attach timestamps
		TimedTrace timedTrace = new TimedTrace();

		double startTime = random.nextInt((int) animationDuration);
		double traceDuration = random.nextInt((int) (animationDuration - startTime));
		timedTrace.setEndTime(timedTrace.getStartTime() + traceDuration);

		int moveIndex = 0;
		double t = 0;
		for (Move move : alignedTrace) {
			if (move.eventClass != null) {
				t = startTime + ((moveIndex + 1.0) / (alignedTrace.size() + 1.0)) * traceDuration;
				timedTrace.add(new TimedMove(move, t));
			} else {
				timedTrace.add(new TimedMove(move, null));
			}
			moveIndex++;
		}

		return timedTrace;
	}

	private static TimedTrace timeTraceFromTimestamps(IMTraceG<Move> alignedTrace, XTrace trace, XLogInfo xLogInfo,
			Pair<Double, Double> extremeTimestamps, InductiveVisualMinerPanel panel) {

		//attach timestamps
		TimedTrace timedTrace = new TimedTrace();
		int xEventIndex = 0;
		double t = 0;
		for (Move move : alignedTrace) {
			if (move.eventClass != null) {
				XEvent event = trace.get(xEventIndex);
				xEventIndex++;

				Double t1 = TimestampsAdder.scale(TimestampsAdder.getTimestamp(event), extremeTimestamps);
				if (t1 != null) {

					//perform sanity check
					if (t1 < t) {
						//this trace is not ordered according to timestamps
						System.out.println("Trace not ordered according to timestamps.");
						return null;
					}

					t = t1;
					timedTrace.add(new TimedMove(move, t));
				} else {
					timedTrace.add(new TimedMove(move, null));
				}
			} else {
				timedTrace.add(new TimedMove(move, null));
			}
		}

		return timedTrace;
	}

	private static List<XEventClass> getTraceLogProjection(XTrace trace, XLogInfo xLogInfo) {
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

	public static double guessStartTime(List<TimedMove> trace, InductiveVisualMinerPanel panel) {
		//find the first timed move
		TimedMove firstTimedMove = null;
		int firstTimedMoveIndex;
		for (firstTimedMoveIndex = 0; firstTimedMoveIndex < trace.size(); firstTimedMoveIndex++) {
			firstTimedMove = trace.get(firstTimedMoveIndex);
			if (firstTimedMove.getTimestamp() != null) {
				break;
			}
		}

		//find the edges the trace is going through after the last timed move
		List<TimedMove> partialTrace = trace.subList(0, firstTimedMoveIndex + 1);

		//the trace ends with 2 seconds per edge
		return firstTimedMove.getTimestamp() - Animation.getEdgesOnMovePath(partialTrace, panel, true, false).size()
				* beginEndEdgeDuration;
	}

	public static double guessEndTime(List<TimedMove> trace, InductiveVisualMinerPanel panel) {
		//find the last timed move
		TimedMove lastTimedMove = null;
		int lastTimedMoveIndex;
		for (lastTimedMoveIndex = trace.size() - 1; lastTimedMoveIndex >= 0; lastTimedMoveIndex--) {
			lastTimedMove = trace.get(lastTimedMoveIndex);
			if (lastTimedMove.getTimestamp() != null) {
				break;
			}
		}

		//find the edges the trace is going through after the last timed move
		List<TimedMove> partialTrace = trace.subList(lastTimedMoveIndex, trace.size());

		//the trace ends with 2 seconds per edge
		return lastTimedMove.getTimestamp() + Animation.getEdgesOnMovePath(partialTrace, panel, false, true).size()
				* beginEndEdgeDuration;
	}

	public static Double scale(Double value, Pair<Double, Double> extremeTimestamps) {
		if (value == null) {
			return null;
		}
		return animationDuration * (value - extremeTimestamps.getLeft())
				/ (extremeTimestamps.getRight() - extremeTimestamps.getLeft());
	}

	public static Double getTimestamp(XEvent event) {
		if (event.hasAttributes() && event.getAttributes().containsKey("time:timestamp")) {
			Date timestamp = ((XAttributeTimestamp) event.getAttributes().get("time:timestamp")).getValue();
			return (double) timestamp.getTime();
		}
		return null;
	}
}
