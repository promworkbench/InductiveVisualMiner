package org.processmining.plugins.inductiveVisualMiner.animation;

import org.processmining.plugins.inductiveVisualMiner.alignment.LogMovePosition;
import org.processmining.plugins.inductiveVisualMiner.alignment.Move;

public class IvMMove extends Move {

	private final Long logTimestamp;
	private final String resource;

	public IvMMove(Move move, Long logTimestamp, String resource) {
		super(move.getType(), move.getUnode(), move.getActivityEventClass(), move.getPerformanceEventClass(), move
				.getLifeCycleTransition());
		setLogMovePosition(LogMovePosition.beforeChild(move.getLogMoveUnode(), move.getLogMoveBeforeChild()));
		setLogMoveParallelBranchMappedTo(move.getLogMoveParallelBranchMappedTo());
		this.logTimestamp = logTimestamp;
		this.resource = resource;
	}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj)) {
			return false;
		}
		return logTimestamp == ((IvMMove) obj).logTimestamp;
	}

	public Double getUserTimestamp(Scaler scaler) {
		return scaler.logTime2UserTime(logTimestamp);
	}

	public Long getLogTimestamp() {
		return logTimestamp;
	}

	public String getResource() {
		return resource;
	}

	public String toString() {
		return super.toString() + " @" + logTimestamp;
	}

	//Event functions from list-view widget
	public String getTopLabel() {
		return TimestampsAdder.toString(logTimestamp);
	}
}
