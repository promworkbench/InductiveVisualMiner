package org.processmining.plugins.inductiveVisualMiner.animation;

import org.processmining.plugins.inductiveVisualMiner.alignment.Move;

public class TimedMove extends Move {
	
	public final Double timestamp;
	
	public TimedMove(Move move, Double timestamp) {
		super(move.type, move.unode, move.eventClass);
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
