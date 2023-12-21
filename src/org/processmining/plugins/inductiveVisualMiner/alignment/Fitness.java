package org.processmining.plugins.inductiveVisualMiner.alignment;

import org.processmining.plugins.inductiveVisualMiner.alignment.Move.Type;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IteratorWithPosition;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFiltered;
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

	public static boolean traceFits(IvMTrace trace) {
		for (IvMMove move : trace) {
			if (move.isComplete() && !move.isIgnoredLogMove() && !move.isIgnoredModelMove()) {
				if (move.getType() != Type.synchronousMove) {
					return false;
				}
			}
		}
		return true;
	}

	public static double compute(IvMLogNotFiltered log) {
		double sum = 0;
		int count = 0;
		for (IteratorWithPosition<IvMTrace> it = log.iterator(); it.hasNext();) {
			IvMTrace trace = it.next();
			sum += Fitness.compute(trace);
			count++;
		}
		if (count == 0) {
			return 1;
		}
		return sum / count;
	}

	public static double compute(IvMLogFiltered log) {
		double sum = 0;
		int count = 0;
		for (IteratorWithPosition<IvMTrace> it = log.iterator(); it.hasNext();) {
			IvMTrace trace = it.next();
			sum += Fitness.compute(trace);
			count++;
		}
		if (count == 0) {
			return 1;
		}
		return sum / count;
	}

	public static int getNumberOfFittingTraces(IvMLogFiltered log) {
		int count = 0;
		for (IteratorWithPosition<IvMTrace> it = log.iterator(); it.hasNext();) {
			IvMTrace trace = it.next();
			if (traceFits(trace)) {
				count++;
			}
		}
		return count;
	}
	
	public static int getNumberOfFittingTraces(IvMLogNotFiltered log) {
		int count = 0;
		for (IteratorWithPosition<IvMTrace> it = log.iterator(); it.hasNext();) {
			IvMTrace trace = it.next();
			if (traceFits(trace)) {
				count++;
			}
		}
		return count;
	}
}
