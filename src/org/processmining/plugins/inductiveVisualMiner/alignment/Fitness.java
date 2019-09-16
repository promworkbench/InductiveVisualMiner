package org.processmining.plugins.inductiveVisualMiner.alignment;

import org.processmining.plugins.inductiveVisualMiner.alignment.Move.Type;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;

public class Fitness {
	public static double compute(IvMTrace trace) {

		int synchronousMoves = 0;
		int moves = 0;

		for (IvMMove move : trace) {
			if (move.isComplete() && !move.isIgnoredLogMove() && !move.isIgnoredModelMove()) {
				if (move.getType() == Type.synchronousMove) {
					synchronousMoves++;
				}
				moves++;
			}
		}
		
		return synchronousMoves / (moves * 1.0);
	}
}
