package org.processmining.plugins.inductiveVisualMiner.animation;

import org.processmining.plugins.inductiveVisualMiner.alignment.LogMovePosition;
import org.processmining.plugins.inductiveVisualMiner.alignment.Move;

public class TimedMove extends Move {
	
	public static class Scaler {
		private final double animationDuration;
		private final long min;
		private final long max;

		public Scaler(final double animationDuration, final long min, final long max) {
			this.min = min;
			this.max = max;
			this.animationDuration = animationDuration;
		}

		public Double scale(Long value) {
			if (value == null) {
				return null;
			}
			if (max == min) {
				return animationDuration * value;
			}
			return animationDuration * (value - min) / (max - 1.0 * min);
		}
	}

	private final Long logTimestamp;

	public TimedMove(Move move, Long logTimestamp, boolean start) {
		super(move.getType(), move.getUnode(), move.getEventClass(), start);
		setLogMove(LogMovePosition.beforeChild(move.getLogMoveUnode(), move.getLogMoveBeforeChild()));
		setLogMoveParallelBranchMappedTo(move.getLogMoveParallelBranchMappedTo());
		this.logTimestamp = logTimestamp;
	}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj)) {
			return false;
		}
		return logTimestamp == ((TimedMove) obj).logTimestamp;
	}
	
	public Double getScaledTimestamp(Scaler scaler) {
		return scaler.scale(logTimestamp);
	}
	
	public Long getLogTimestamp() {
		return logTimestamp;
	}

	public String toString() {
		return super.toString() + " " + (isStart() ? "start" : "complete") + " @" + logTimestamp;
	}
	
	//Event functions from list-view widget
	public String getTopLabel() {
		return TimestampsAdder.toString(logTimestamp);
	}
}
