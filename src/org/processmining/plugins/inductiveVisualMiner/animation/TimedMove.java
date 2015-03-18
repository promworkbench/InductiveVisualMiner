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
		
		public Long scaleBack(Double value2) {
			if (value2 == null) {
				return null;
			}
			if (max == min) {
				return (long) (value2 / animationDuration);
			}
			return (long) ((value2 / (1.0 * animationDuration)) * (max - 1.0 * min) + min);
		}
		
		public long getMin() {
			return min;
		}
		
		public long getMax() {
			return max;
		}
	}

	private final Long logTimestamp;

	public TimedMove(Move move, Long logTimestamp) {
		super(move.getType(), move.getUnode(), move.getActivityEventClass(), move.getPerformanceEventClass(), move.isStart(), move.isTauStart());
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
		return super.toString() + " @" + logTimestamp;
	}
	
	//Event functions from list-view widget
	public String getTopLabel() {
		return TimestampsAdder.toString(logTimestamp);
	}
}
