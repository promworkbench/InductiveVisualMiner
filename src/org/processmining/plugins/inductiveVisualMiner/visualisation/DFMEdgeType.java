package org.processmining.plugins.inductiveVisualMiner.visualisation;

import org.processmining.plugins.inductiveVisualMiner.alignment.LogMovePosition;

public enum DFMEdgeType {
	/**
	 * normal DFM-edge
	 */
	modelBetweenActivities,

	/**
	 * DFM-edge from an activity to itself, e.g. a_start, a_complete, a_start,
	 * a_complete
	 */
	modelSelfLoop,

	/**
	 * log move edge during an activity, e.g. a_start, log move, a_complete
	 */
	logMoveDuringActivity,

	/**
	 * log move edge in between two different activities, e.g. a_start,
	 * a_complete, log move, b_start, b_complete
	 */
	logMoveBetweenActivities,

	/**
	 * log move edge in between two executions of the same activity, e.g.
	 * a_start, a_complete, log move, a_start, a_complete
	 */
	logMoveOnSelfLoop,

	/**
	 * model move edge
	 */
	modelMove,

	/**
	 * edge from a point where a model move edge splits off to/from an activity
	 */
	modelIntraActivity,

	/**
	 * Edge denoting the empty trace in a model
	 */
	modelEmptyTraces;

	private boolean canHaveLogMoves = false;
	static {
		modelBetweenActivities.canHaveLogMoves = true;
		modelSelfLoop.canHaveLogMoves = true;
		modelEmptyTraces.canHaveLogMoves = true;
	}

	public boolean canHaveLogMoves() {
		return canHaveLogMoves;
	}

	private boolean isLogMove = false;
	static {
		logMoveDuringActivity.isLogMove = true;
		logMoveBetweenActivities.isLogMove = true;
		logMoveOnSelfLoop.isLogMove = true;
	}

	public boolean isLogMove() {
		return isLogMove;
	}

	private boolean frequencyIncludesModelMoves = true;
	static {
		modelIntraActivity.frequencyIncludesModelMoves = false;
	}

	public boolean isFrequencyIncludesModelMoves() {
		return frequencyIncludesModelMoves;
	}

	private DFMEdgeType correspondingLogMove = null;
	static {
		modelBetweenActivities.correspondingLogMove = logMoveBetweenActivities;
		modelSelfLoop.correspondingLogMove = logMoveOnSelfLoop;
	}

	public DFMEdgeType getCorrespondingLogMove() {
		return correspondingLogMove;
	}

	public LogMovePosition getLogMovePosition(int dfmNodeFrom, int dfmNodeTo) {
		switch (this) {
			case logMoveDuringActivity :
				return null;
			case logMoveBetweenActivities :
				return null;
			case logMoveOnSelfLoop :
				return null;
			case modelBetweenActivities :
				return LogMovePosition.onEdge(dfmNodeFrom, dfmNodeTo);
			case modelIntraActivity :
				return null;
			case modelMove :
				return null;
			case modelSelfLoop :
				return LogMovePosition.betweenTwoExecutionsOf(dfmNodeTo);
			case modelEmptyTraces :
				return LogMovePosition.onEdge(-1, -1);
			default :
				return null;
		}
	}
}
