package org.processmining.plugins.inductiveVisualMiner.popup;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.inductiveVisualMiner.alignment.LogMovePosition;

public class PopupItemInputLogMove implements PopupItemInput<PopupItemInputLogMove> {

	private final LogMovePosition position;
	private final MultiSet<XEventClass> logMoves;

	public PopupItemInputLogMove(LogMovePosition position, MultiSet<XEventClass> logMoves) {
		this.position = position;
		this.logMoves = logMoves;
	}

	public PopupItemInputLogMove get() {
		return this;
	}

	public LogMovePosition getPosition() {
		return position;
	}

	public MultiSet<XEventClass> getLogMoves() {
		return logMoves;
	}
}