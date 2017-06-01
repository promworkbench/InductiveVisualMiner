package org.processmining.plugins.inductiveVisualMiner.animation;

import org.processmining.plugins.inductiveVisualMiner.chain.IvMCanceller;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLog;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;

public class Scaler {
	private final double animationDurationInUserTime; //the duration of the animation in seconds
	private final double initialisationInLogTime; //the time a trace spends before reaching the first node, and the time from the last activity to the end
	private final double minInLogTime; // the minimum log time that will be encountered in the animation (including fade-in).
	private final double maxInLogTime; // the maximum log time that will be encountered in the animation (including fade-out).
	private final boolean correctTime; //denotes whether the time comes from the event log or is randomly generated

	public static Scaler fromLog(final IvMLog log, final double fadeDurationInUserTime,
			final double animationDurationInUserTime, final IvMCanceller canceller) {
		double logMin = Long.MAX_VALUE;
		double logMax = Long.MIN_VALUE;
		for (IvMTrace trace : log) {
			for (IvMMove move : trace) {
				if (move.getLogTimestamp() != null) {
					logMin = Math.min(logMin, move.getLogTimestamp());
					logMax = Math.max(logMax, move.getLogTimestamp());

					if (trace.getRealStartTime() == null) {
						trace.setRealStartTime(move.getLogTimestamp());
					}
					trace.setRealEndTime(move.getLogTimestamp());
				}
			}
			if (canceller.isCancelled()) {
				return null;
			}
		}

		if (logMin == Long.MAX_VALUE || logMax == Long.MIN_VALUE) {
			return null;
		}

		//account for the fading time
		double logDurationInLogTime;
		if (logMin == logMax) {
			//singular timestamp
			//set an arbitrary time for the initialisation
			logDurationInLogTime = 10;
		} else {
			logDurationInLogTime = logMax - logMin;
		}
		double initialisationInLogTime = (logDurationInLogTime * fadeDurationInUserTime)
				/ (animationDurationInUserTime - 2 * fadeDurationInUserTime);

		return new Scaler(animationDurationInUserTime, initialisationInLogTime, logMin - initialisationInLogTime,
				logMax + initialisationInLogTime, true);
	}

	public static Scaler fromValues(final double animationDuration) {
		return new Scaler(animationDuration, 0, 0, animationDuration, false);
	}

	private Scaler(final double animationDuration, double initialisationInLogTime, final double minInLogTime,
			final double maxInLogTime, boolean correctTime) {
		this.minInLogTime = minInLogTime;
		this.maxInLogTime = maxInLogTime;
		this.animationDurationInUserTime = animationDuration;
		this.initialisationInLogTime = initialisationInLogTime;
		this.correctTime = correctTime;
	}

	/**
	 * 
	 * @param logTime
	 *            The time in log timestamps.
	 * @return The time in user time (from 0 to the duration of the animation in
	 *         seconds)
	 */
	public Double logTime2UserTime(Double logTime) {
		if (logTime == null) {
			return null;
		}
		if (maxInLogTime == minInLogTime) {
			return animationDurationInUserTime * logTime;
		}
		return animationDurationInUserTime * (logTime - minInLogTime) / (maxInLogTime - 1.0 * minInLogTime);
	}

	public Double logTime2UserTime(Long logTime) {
		if (logTime == null) {
			return null;
		}
		if (maxInLogTime == minInLogTime) {
			return animationDurationInUserTime / 2;
		}
		return animationDurationInUserTime * (logTime - minInLogTime) / (maxInLogTime - 1.0 * minInLogTime);
	}

	public Double userTime2LogTime(Double userTime) {
		if (userTime == null) {
			return null;
		}
		if (maxInLogTime == minInLogTime) {
			return userTime / animationDurationInUserTime;
		}
		return (userTime / (1.0 * animationDurationInUserTime)) * (maxInLogTime - 1.0 * minInLogTime) + minInLogTime;
	}

	public Double userTime2Fraction(Double userTime) {
		Double logTime = userTime2LogTime(userTime);
		if (logTime == null) {
			return null;
		}
		if (maxInLogTime == minInLogTime) {
			return logTime / animationDurationInUserTime;
		}
		return (logTime - minInLogTime) / (maxInLogTime - 1.0 * minInLogTime);
	}

	public double getMinInUserTime() {
		return 0;
	}

	public double getMaxInUserTime() {
		return animationDurationInUserTime;
	}

	public double getMinInLogTime() {
		return minInLogTime;
	}

	public double getMaxInLogTime() {
		return maxInLogTime;
	}

	public double getInitialisationInLogTime() {
		return initialisationInLogTime;
	}

	/**
	 * 
	 * @return whether the timing is derived from the log (true) or randomly
	 *         generated (false).
	 */
	public boolean isCorrectTime() {
		return correctTime;
	}
}