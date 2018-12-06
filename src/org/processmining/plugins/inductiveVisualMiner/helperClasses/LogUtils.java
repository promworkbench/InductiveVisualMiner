package org.processmining.plugins.inductiveVisualMiner.helperClasses;

import java.util.List;

import org.processmining.plugins.inductiveVisualMiner.alignment.Move;

public class LogUtils {

	/**
	 * 
	 * @param trace
	 * @param index
	 * @return The next model/sync move in trace after index, or null if no such
	 *         move exists.
	 */
	public static Move findNextCompleteModelMove(List<Move> trace, int index) {
		for (int i = index + 1; i < trace.size(); i++) {
			Move next = trace.get(i);
			if (next.isModelSync() && next.isComplete()) {
				return next;
			}
		}
		return null;
	}

	/**
	 * 
	 * @param trace
	 * @param index
	 * @return The previous model/sync move in trace before index, or null if no
	 *         such move exists.
	 */
	public static Move findPreviousCompleteModelMove(List<Move> trace, int index) {
		for (int i = index - 1; i >= 0; i--) {
			Move next = trace.get(i);
			if (next.isModelSync() && next.isComplete()) {
				return next;
			}
		}
		return null;
	}
}
