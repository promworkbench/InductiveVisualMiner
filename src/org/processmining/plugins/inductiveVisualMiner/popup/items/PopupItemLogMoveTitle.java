package org.processmining.plugins.inductiveVisualMiner.popup.items;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInput;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInputLogMove;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemLogMove;

public class PopupItemLogMoveTitle implements PopupItemLogMove {

	public String[][] get(InductiveVisualMinerState state, PopupItemInput<PopupItemInputLogMove> input) {
		if (state.isAlignmentReady()) {
			MultiSet<XEventClass> logMoves = input.get().getLogMoves();
			return new String[][] {
					{ logMoves.size() + (logMoves.size() <= 1 ? " event" : " events") + " additional to the model:" } };
		} else {
			return nothing;
		}
	}

}