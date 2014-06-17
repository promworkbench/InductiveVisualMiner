package org.processmining.plugins.inductiveVisualMiner.animation;

import org.processmining.plugins.inductiveVisualMiner.alignment.Move;

public class TimedMove extends Move {
	
	private final Double timestamp;
	
	public TimedMove(Move move, Double timestamp) {
		super(move.getType(), move.getUnode(), move.getEventClass());
		setLogMove(move.getLogMoveUnode(), move.getLogMoveBeforeChild());
		setLogMoveParallelBranchMappedTo(move.getLogMoveParallelBranchMappedTo());
		this.timestamp = timestamp;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj)) {
			return false;
		}
		return timestamp == ((TimedMove) obj).timestamp;
	}

	public Double getTimestamp() {
		return timestamp;
	}
	
	public String toString() {
		return super.toString() + " @" + timestamp;
	}
	
}
