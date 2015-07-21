package org.processmining.plugins.inductiveVisualMiner.ivmlog;

import org.deckfour.xes.model.XAttributeMap;
import org.processmining.plugins.inductiveVisualMiner.alignment.LogMovePosition;
import org.processmining.plugins.inductiveVisualMiner.alignment.Move;
import org.processmining.plugins.inductiveVisualMiner.animation.Scaler;
import org.processmining.plugins.inductiveVisualMiner.animation.TimestampsAdder;

public class IvMMove extends Move {

	private final Long logTimestamp;
	private final String resource;
	private final XAttributeMap attributes;

	public IvMMove(Move move, Long logTimestamp, String resource, XAttributeMap xAttributeMap) {
		super(move.getType(), move.getUnode(), move.getActivityEventClass(), move.getPerformanceEventClass(), move
				.getLifeCycleTransition());
		setLogMovePosition(LogMovePosition.beforeChild(move.getLogMoveUnode(), move.getLogMoveBeforeChild()));
		setLogMoveParallelBranchMappedTo(move.getLogMoveParallelBranchMappedTo());
		this.logTimestamp = logTimestamp;
		this.resource = resource;
		this.attributes = xAttributeMap;
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
	
	public XAttributeMap getAttributes() {
		return attributes;
	}

	//Event functions from list-view widget
	public String getTopLabel() {
		return TimestampsAdder.toString(logTimestamp);
	}
}
